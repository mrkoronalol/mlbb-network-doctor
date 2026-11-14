package ru.mlbb.blocker.vpn

import io.nekohasekai.libbox.CommandClient
import io.nekohasekai.libbox.CommandClientHandler
import io.nekohasekai.libbox.CommandClientOptions
import io.nekohasekai.libbox.ConnectionEvents
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.LogIterator
import io.nekohasekai.libbox.OutboundGroupItemIterator
import io.nekohasekai.libbox.OutboundGroupIterator
import io.nekohasekai.libbox.StatusMessage
import io.nekohasekai.libbox.StringIterator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.mlbb.blocker.data.BlockerPolicy
import ru.mlbb.blocker.data.GamePorts
import ru.mlbb.blocker.data.MatchPhase
import ru.mlbb.blocker.data.RouteAction
import ru.mlbb.blocker.data.RuleEngine
import ru.mlbb.blocker.data.db.MonitorRepository
import ru.mlbb.blocker.data.logs.FileLog
import ru.mlbb.blocker.data.trace.DebugTrace
import ru.mlbb.blocker.data.trace.TraceEntry
class ConnectionMonitor(
    private val scope: CoroutineScope,
    private val log: (String) -> Unit,
    private val onMatch: ((String, Int, String, Boolean) -> Unit)? = null,
    private val onConnection: ((String, Int, String) -> Unit)? = null,
    private val debugTrace: () -> Boolean = { false },
    private val phaseProvider: () -> MatchPhase = { MatchPhase.IDLE },
    private val policyProvider: () -> BlockerPolicy = { BlockerPolicy() },
) {
    private var client: CommandClient? = null
    private var connectJob: Job? = null
    private var reconnectJob: Job? = null
    private var stopped = true

    fun start() {
        stopped = false
        if (client != null || connectJob != null || reconnectJob?.isActive == true) return
        connectJob = scope.launch(Dispatchers.IO) {
            val options = CommandClientOptions().apply {
                addCommand(Libbox.CommandConnections)
                statusInterval = 1_000_000_000L
            }
            try {
                val newClient = CommandClient(createHandler(), options)
                client = newClient
                newClient.connect()
            } catch (t: Throwable) {
                log("Мониторинг не запустился: ${t.message ?: t.javaClass.simpleName}")
                client = null
                scheduleReconnect()
            } finally {
                connectJob = null
            }
        }
    }

    fun stop() {
        stopped = true
        reconnectJob?.cancel()
        reconnectJob = null
        connectJob?.cancel()
        connectJob = null
        val current = client
        client = null
        if (current != null) {
            scope.launch(Dispatchers.IO) {
                runCatching { current.disconnect() }
            }
        }
    }

    private fun scheduleReconnect() {
        if (stopped) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch(Dispatchers.IO) {
            delay(2500)
            reconnectJob = null
            if (!stopped) {
                start()
            }
        }
    }

    private fun createHandler(): CommandClientHandler {
        return object : CommandClientHandler {
            override fun connected() {
                reconnectJob?.cancel()
                reconnectJob = null
                log("Мониторинг IP подключён: собираются все подключения MLBB")
            }

            override fun disconnected(message: String?) {
                if (!message.isNullOrBlank()) {
                    log("Мониторинг отключён: $message")
                }
                client = null
                if (!stopped) {
                    scheduleReconnect()
                }
            }

            override fun writeConnectionEvents(events: ConnectionEvents?) {
                if (events == null) return
                try {
                    val iterator = events.iterator()
                    while (iterator.hasNext()) {
                        val event = iterator.next()
                        val type = event.type.toLong()
                        if (type != Libbox.ConnectionEventNew && type != Libbox.ConnectionEventUpdate) continue
                        val connection = event.connection ?: continue
                        val destination = connection.destination
                            ?: connection.displayDestination()
                            ?: continue
                        val parsed = parseDestination(destination) ?: continue
                        val network = connection.network?.takeIf { it.isNotBlank() } ?: "tcp"
                        val host = connection.domain?.takeIf { it.isNotBlank() }
                        val timestamp = normalizeTimestamp(connection.createdAt)
                        val decision = RuleEngine.decide(
                            parsed.first,
                            parsed.second,
                            policyProvider(),
                        )
                        val blocked = decision.action == RouteAction.SINK
                        if (blocked) {
                            FileLog.write("block", "rule=${decision.matchedRule ?: decision.reason} $parsed")
                            MonitorRepository.recordBlockedAttempt(parsed.first, parsed.second, network, "reject")
                        } else {
                            MonitorRepository.record(parsed.first, parsed.second, network, host, timestamp)
                        }
                        recordTrace(parsed.first, parsed.second, network, blocked, connection.fromOutbound)
                        if (GamePorts.isMatchPort(parsed.second)) {
                            onMatch?.invoke(parsed.first, parsed.second, network, blocked)
                        }
                        if (onConnection != null) {
                            runCatching { onConnection.invoke(parsed.first, parsed.second, network) }
                        }
                    }
                } catch (_: Throwable) {
                    // ignore malformed events
                }
            }

            override fun clearLogs() = Unit

            override fun setDefaultLogLevel(level: Int) = Unit

            override fun updateClashMode(newMode: String) = Unit

            override fun initializeClashMode(modeList: StringIterator, currentMode: String) = Unit

            override fun writeGroups(message: OutboundGroupIterator?) = Unit

            override fun writeLogs(messageList: LogIterator?) = Unit

            override fun writeOutbounds(message: OutboundGroupItemIterator?) = Unit

            override fun writeStatus(message: StatusMessage) = Unit
        }
    }

    private fun recordTrace(ip: String, port: Int, network: String, blocked: Boolean, fromOutbound: String) {
        // Постоянный лог: каждое соединение MLBB пишется в файл всегда.
        FileLog.write(
            tag = "conn",
            message = "$network $ip:$port out=${fromOutbound.ifBlank { "?" }} " +
                "phase=${phaseProvider().name} ${if (blocked) "BLOCKED" else "ok"}",
        )
        if (!debugTrace()) return
        val phase = phaseProvider().name
        val category = when {
            blocked -> "block"
            fromOutbound == "delay-relay" -> "delay"
            network == "udp" && phase == "QUEUE" -> "udp_probe"
            network == "tcp" && phase == "QUEUE" -> "tcp_handshake"
            else -> "contact"
        }
        DebugTrace.record(
            TraceEntry(
                ts = System.currentTimeMillis(),
                phase = phase,
                category = category,
                ip = ip,
                port = port,
                network = network,
            ),
        )
    }

    private fun parseDestination(destination: String): Pair<String, Int>? {        val value = destination.trim()
        if (value.isEmpty()) return null
        if (value.startsWith("[")) {
            val end = value.indexOf(']')
            if (end < 0) return null
            val ip = value.substring(1, end)
            val port = value.substring(end + 1).removePrefix(":").toIntOrNull() ?: return null
            if (ip.isBlank() || port <= 0) return null
            return ip to port
        }
        val separator = value.lastIndexOf(':')
        if (separator <= 0) return null
        val ip = value.substring(0, separator)
        val port = value.substring(separator + 1).toIntOrNull() ?: return null
        if (ip.isBlank() || port <= 0) return null
        return ip to port
    }

    private fun normalizeTimestamp(value: Long): Long {
        if (value <= 0) return System.currentTimeMillis()
        return if (value < 10_000_000_000L) value * 1000 else value
    }
}
