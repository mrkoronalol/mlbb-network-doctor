package ru.mlbb.blocker.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.mlbb.blocker.data.BlockerRepository
import ru.mlbb.blocker.data.ShaperStats
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicLong

/**
 * Локальный релей задержки для «плохих» IP (этап 2).
 *
 * Минимальный SOCKS5-сервер (RFC 1928):
 * - TCP CONNECT: задерживает каждый чанк клиент→сервер на [delayMs] и пересылает.
 * - UDP ASSOCIATE: задерживает каждую датаграмму перед отправкой на цель.
 *
 * Релей живёт в процессе приложения (UID не включён в TUN), поэтому исходящие
 * сокеты релея идут напрямую — protect() не нужен. sing-box подключается к релею
 * outbound'ом `socks` на 127.0.0.1.
 *
 * Задерживается только UDP-игровой трафик к нежелательным регионам; логин-пул и
 * сервисные порты в правилах идут раньше и в релей не попадают.
 */
class DelayShaper(private val delayMs: () -> Int) {

    @Volatile
    private var running = false

    private var tcpServer: ServerSocket? = null
    private var udpSocket: DatagramSocket? = null

    private val connections = CopyOnWriteArraySet<Socket>()
    private val clientsByTarget = ConcurrentHashMap<String, MutableSet<ClientAddr>>()

    private val packetsDelayed = AtomicLong(0)
    private val totalDelayMs = AtomicLong(0)
    private val activeFlows = AtomicLong(0)

    private val _stats = MutableStateFlow(ShaperStats())
    val stats: StateFlow<ShaperStats> = _stats.asStateFlow()

    val tcpPort: Int get() = tcpServer?.localPort ?: 0
    val udpPort: Int get() = udpSocket?.localPort ?: 0

    /** Запускает TCP- и UDP-части на 127.0.0.1, возвращает пару (tcpPort, udpPort). */
    fun start(): Pair<Int, Int> {
        stop()
        val tcp = ServerSocket(0, 64, InetAddress.getByName("127.0.0.1"))
        val udp = DatagramSocket(0, InetAddress.getByName("127.0.0.1"))
        tcpServer = tcp
        udpSocket = udp
        running = true

        val acceptThread = Thread({ acceptLoop(tcp) }, "delay-shaper-accept")
        acceptThread.isDaemon = true
        acceptThread.start()

        val udpThread = Thread({ udpLoop(udp) }, "delay-shaper-udp")
        udpThread.isDaemon = true
        udpThread.start()

        publishStats()
        BlockerRepository.log("Шейпер задержки запущен: TCP ${tcp.localPort}, UDP ${udp.localPort}")
        return tcp.localPort to udp.localPort
    }

    fun stop() {
        running = false
        runCatching { tcpServer?.close() }
        runCatching { udpSocket?.close() }
        tcpServer = null
        udpSocket = null
        connections.forEach { runCatching { it.close() } }
        connections.clear()
        clientsByTarget.clear()
        publishStats()
    }

    // --- TCP ---

    private fun acceptLoop(server: ServerSocket) {
        while (running) {
            val client = try {
                server.accept()
            } catch (_: IOException) {
                return
            }
            connections.add(client)
            activeFlows.incrementAndGet()
            publishStats()
            val thread = Thread({ handleTcp(client) }, "delay-shaper-tcp")
            thread.isDaemon = true
            thread.start()
        }
    }

    private fun handleTcp(client: Socket) {
        try {
            client.tcpNoDelay = true
            val input = DataInputStream(client.getInputStream())
            val output = DataOutputStream(client.getOutputStream())

            // Greeting: VER NMETHODS METHODS...
            val version = input.readUnsignedByte()
            if (version != 5) return
            val methods = input.readUnsignedByte()
            repeat(methods) { input.readUnsignedByte() }
            output.write(byteArrayOf(5, 0)); output.flush()

            // Request: VER CMD RSV ATYP DST.ADDR DST.PORT
            input.readUnsignedByte() // ver
            val command = input.readUnsignedByte()
            input.readUnsignedByte() // rsv
            val atyp = input.readUnsignedByte()
            val host = readHost(input, atyp)
            val port = input.readUnsignedShort()

            when (command) {
                1 -> {
                    if (host == null || port <= 0) return
                    connectRelay(client, input, output, InetSocketAddress(InetAddress.getByName(host), port))
                }

                // UDP ASSOCIATE: адрес клиента неважен (часто 0.0.0.0:0).
                3 -> udpAssociate(client, input, output)
                else -> return
            }
        } catch (_: IOException) {
            // клиент закрыл соединение
        } finally {
            connections.remove(client)
            runCatching { client.close() }
            activeFlows.decrementAndGet()
            publishStats()
        }
    }

    private fun connectRelay(
        client: Socket,
        input: DataInputStream,
        output: DataOutputStream,
        target: InetSocketAddress,
    ) {
        val outbound = Socket()
        try {
            outbound.tcpNoDelay = true
            outbound.connect(target, 8000)
        } catch (_: IOException) {
            output.write(byteArrayOf(5, 5, 0, 1, 0, 0, 0, 0, 0, 0))
            output.flush()
            return
        }
        connections.add(outbound)
        // Success: VER REP RSV ATYP BND.ADDR BND.PORT
        output.write(byteArrayOf(5, 0, 0, 1, 0, 0, 0, 0, 0, 0))
        output.flush()

        val upstream = Thread({ pump(client.getInputStream(), outbound.getOutputStream(), withDelay = true) }, "delay-up")
        val downstream = Thread({ pump(outbound.getInputStream(), client.getOutputStream(), withDelay = false) }, "delay-down")
        upstream.isDaemon = true
        downstream.isDaemon = true
        upstream.start()
        downstream.start()
        upstream.join()
        runCatching { outbound.close() }
    }

    private fun udpAssociate(client: Socket, input: DataInputStream, output: DataOutputStream) {
        val port = udpPort
        if (port <= 0) return
        output.write(byteArrayOf(5, 0, 0, 1, 127, 0, 0, 1, (port shr 8).toByte(), (port and 0xff).toByte()))
        output.flush()
        // Держим control-канал, пока клиент не закроет его.
        val buffer = ByteArray(512)
        while (running) {
            val read = try {
                input.read(buffer)
            } catch (_: IOException) {
                break
            }
            if (read < 0) break
        }
    }

    private fun pump(
        source: java.io.InputStream,
        sink: java.io.OutputStream,
        withDelay: Boolean,
    ) {
        val buffer = ByteArray(16 * 1024)
        try {
            while (running) {
                val read = source.read(buffer)
                if (read < 0) break
                if (withDelay) delayPacket()
                sink.write(buffer, 0, read)
                sink.flush()
            }
        } catch (_: IOException) {
            // соединение закрыто
        } finally {
            runCatching { source.close() }
            runCatching { sink.close() }
        }
    }

    // --- UDP ---

    private fun udpLoop(udp: DatagramSocket) {
        val buffer = ByteArray(64 * 1024)
        while (running) {
            val packet = DatagramPacket(buffer, buffer.size)
            try {
                udp.receive(packet)
            } catch (_: IOException) {
                return
            }
            handleUdp(udp, packet)
        }
    }

    private fun handleUdp(udp: DatagramSocket, packet: DatagramPacket) {
        val data = packet.data
        val length = packet.length
        val sourceAddress = packet.address ?: return
        val sourcePort = packet.port

        // Ответ от цели? Тогда источник совпадает с зарегистрированной целью.
        val sourceKey = "${sourceAddress.hostAddress}:$sourcePort"
        val clients = clientsByTarget[sourceKey]
        if (clients != null) {
            for (client in clients) {
                val wrapped = wrapUdpReply(sourceAddress, sourcePort, data, length) ?: continue
                try {
                    udp.send(DatagramPacket(wrapped, wrapped.size, client.address, client.port))
                } catch (_: IOException) {
                    clients.remove(client)
                }
            }
            if (clients.isEmpty()) clientsByTarget.remove(sourceKey)
            return
        }

        // Запрос клиента → цель. Формат: RSV(2) FRAG(1) ATYP(1) DST.ADDR DST.PORT DATA
        if (length < 10) return
        if (data[0] != 0.toByte() || data[1] != 0.toByte() || data[2] != 0.toByte()) return
        val target = parseUdpTarget(data, length) ?: return
        val payload = data.copyOfRange(target.offset, length)

        delayPacket()
        try {
            udp.send(DatagramPacket(payload, payload.size, target.address, target.port))
        } catch (_: IOException) {
            return
        }
        val key = "${target.address.hostAddress}:${target.port}"
        val set = clientsByTarget.getOrPut(key) { mutableSetOf() }
        set.add(ClientAddr(sourceAddress, sourcePort))
    }

    private fun parseUdpTarget(data: ByteArray, length: Int): UdpTarget? {
        val atyp = data[3].toInt() and 0xff
        var offset = 4
        val host: String = when (atyp) {
            1 -> {
                if (length < offset + 4) return null
                val host = "${data[offset].toInt() and 0xff}.${data[offset + 1].toInt() and 0xff}." +
                    "${data[offset + 2].toInt() and 0xff}.${data[offset + 3].toInt() and 0xff}"
                offset += 4
                host
            }

            4 -> {
                if (length < offset + 16) return null
                val bytes = ByteArray(16) { data[offset + it] }
                offset += 16
                InetAddress.getByAddress(bytes).hostAddress ?: return null
            }

            3 -> {
                val len = data[offset].toInt() and 0xff
                offset += 1
                if (length < offset + len) return null
                val host = String(data, offset, len, Charsets.US_ASCII)
                offset += len
                host
            }

            else -> return null
        }
        if (length < offset + 2) return null
        val port = ((data[offset].toInt() and 0xff) shl 8) or (data[offset + 1].toInt() and 0xff)
        offset += 2
        if (port <= 0) return null
        return try {
            UdpTarget(InetAddress.getByName(host), port, offset)
        } catch (_: Exception) {
            null
        }
    }

    private fun wrapUdpReply(source: InetAddress, sourcePort: Int, data: ByteArray, length: Int): ByteArray? {
        val payload = data.copyOfRange(0, length)
        val header = ByteArray(10)
        header[0] = 0; header[1] = 0; header[2] = 0
        if (source is Inet4Address) {
            header[3] = 1
            System.arraycopy(source.address, 0, header, 4, 4)
        } else {
            return null // IPv6-цели не поддерживаются релеем
        }
        header[8] = (sourcePort shr 8).toByte()
        header[9] = (sourcePort and 0xff).toByte()
        return header + payload
    }

    private fun readHost(input: DataInputStream, atyp: Int): String? {
        return when (atyp) {
            1 -> {
                val bytes = ByteArray(4) { input.readByte() }
                InetAddress.getByAddress(bytes).hostAddress
            }

            4 -> {
                val bytes = ByteArray(16) { input.readByte() }
                InetAddress.getByAddress(bytes).hostAddress
            }

            3 -> {
                val len = input.readUnsignedByte()
                val bytes = ByteArray(len) { input.readByte() }
                String(bytes, Charsets.US_ASCII)
            }

            else -> null
        }
    }

    private fun delayPacket() {
        val ms = delayMs().coerceIn(0, 2_000)
        if (ms > 0) {
            try {
                Thread.sleep(ms.toLong())
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        packetsDelayed.incrementAndGet()
        totalDelayMs.addAndGet(ms.toLong())
        publishStats()
    }

    private fun publishStats() {
        val delayed = packetsDelayed.get()
        val total = totalDelayMs.get()
        val stats = ShaperStats(
            packetsDelayed = delayed,
            avgDelayMs = if (delayed == 0L) 0L else total / delayed,
            activeFlows = activeFlows.get().coerceAtLeast(0).toInt(),
        )
        _stats.value = stats
        BlockerRepository.setShaperStats(stats)
    }

    private data class ClientAddr(val address: InetAddress, val port: Int)
    private data class UdpTarget(val address: InetAddress, val port: Int, val offset: Int)
}
