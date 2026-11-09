package ru.mlbb.blocker

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.mlbb.blocker.vpn.DelayShaper
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class DelayShaperTest {

    private fun readExactly(input: InputStream, count: Int): ByteArray {
        val buffer = ByteArray(count)
        var offset = 0
        while (offset < count) {
            val read = input.read(buffer, offset, count - offset)
            if (read < 0) throw IllegalStateException("EOF after $offset bytes")
            offset += read
        }
        return buffer
    }

    private fun socksGreeting(input: DataInputStream, output: DataOutputStream) {
        output.write(byteArrayOf(5, 1, 0))
        output.flush()
        assertArrayEquals(byteArrayOf(5, 0), readExactly(input, 2))
    }

    @Test
    fun tcpConnectRelaysWithDelay() {
        val delay = 150
        val shaper = DelayShaper { delay }
        val (tcpPort, _) = shaper.start()

        val echo = ServerSocket(0, 10, InetAddress.getByName("127.0.0.1"))
        val echoThread = Thread {
            val client = echo.accept()
            val input = client.getInputStream()
            val output = client.getOutputStream()
            val buffer = ByteArray(128)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                output.write(buffer, 0, read)
                output.flush()
            }
        }
        echoThread.isDaemon = true
        echoThread.start()

        Socket().use { client ->
            client.connect(InetSocketAddress("127.0.0.1", tcpPort), 3000)
            client.soTimeout = 5000
            val input = DataInputStream(client.getInputStream())
            val output = DataOutputStream(client.getOutputStream())

            socksGreeting(input, output)

            val port = echo.localPort
            output.write(
                byteArrayOf(5, 1, 0, 1, 127, 0, 0, 1, (port shr 8).toByte(), (port and 0xff).toByte()),
            )
            output.flush()
            val reply = readExactly(input, 10)
            assertEquals(0, reply[1].toInt())

            val payload = "ping".toByteArray()
            val start = System.nanoTime()
            output.write(payload)
            output.flush()
            val echoed = readExactly(input, payload.size)
            val elapsedMs = (System.nanoTime() - start) / 1_000_000

            assertArrayEquals(payload, echoed)
            assertTrue("elapsedMs=$elapsedMs", elapsedMs >= 120)
        }

        assertTrue(shaper.stats.value.packetsDelayed >= 1)
        shaper.stop()
        echo.close()
    }

    @Test
    fun udpAssociateRelaysWithDelay() {
        val delay = 150
        val shaper = DelayShaper { delay }
        val (tcpPort, _) = shaper.start()

        val echoUdp = DatagramSocket(0, InetAddress.getByName("127.0.0.1"))
        val udpThread = Thread {
            val buffer = ByteArray(2048)
            while (true) {
                val packet = DatagramPacket(buffer, buffer.size)
                echoUdp.receive(packet)
                echoUdp.send(DatagramPacket(packet.data, packet.length, packet.address, packet.port))
            }
        }
        udpThread.isDaemon = true
        udpThread.start()

        Socket().use { control ->
            control.connect(InetSocketAddress("127.0.0.1", tcpPort), 3000)
            control.soTimeout = 5000
            val input = DataInputStream(control.getInputStream())
            val output = DataOutputStream(control.getOutputStream())

            socksGreeting(input, output)

            // UDP ASSOCIATE: bind 0.0.0.0:0
            output.write(byteArrayOf(5, 3, 0, 1, 0, 0, 0, 0, 0, 0))
            output.flush()
            val reply = readExactly(input, 10)
            assertEquals(0, reply[1].toInt())
            val relayPort = ((reply[8].toInt() and 0xff) shl 8) or (reply[9].toInt() and 0xff)
            assertTrue(relayPort > 0)

            val clientUdp = DatagramSocket(0, InetAddress.getByName("127.0.0.1"))
            clientUdp.soTimeout = 5000

            val echoPort = echoUdp.localPort
            val header = byteArrayOf(
                0, 0, 0, 1, 127, 0, 0, 1,
                (echoPort shr 8).toByte(), (echoPort and 0xff).toByte(),
            )
            val payload = header + "hello".toByteArray()
            val start = System.nanoTime()
            clientUdp.send(
                DatagramPacket(payload, payload.size, InetAddress.getByName("127.0.0.1"), relayPort),
            )

            val buffer = ByteArray(2048)
            val received = DatagramPacket(buffer, buffer.size)
            clientUdp.receive(received)
            val elapsedMs = (System.nanoTime() - start) / 1_000_000

            val data = String(buffer, 10, received.length - 10, Charsets.US_ASCII)
            assertEquals("hello", data)
            assertTrue("elapsedMs=$elapsedMs", elapsedMs >= 120)

            clientUdp.close()
        }

        assertTrue(shaper.stats.value.packetsDelayed >= 1)
        shaper.stop()
        echoUdp.close()
    }

    @Test
    fun stopClosesSocketsAndAllowsRestart() {
        val shaper = DelayShaper { 100 }
        val (tcpPort, udpPort) = shaper.start()
        assertTrue(tcpPort > 0)
        assertTrue(udpPort > 0)

        shaper.stop()
        assertEquals(0, shaper.tcpPort)

        val (tcpPort2, udpPort2) = shaper.start()
        assertTrue(tcpPort2 > 0)
        assertTrue(udpPort2 > 0)
        shaper.stop()
    }
}
