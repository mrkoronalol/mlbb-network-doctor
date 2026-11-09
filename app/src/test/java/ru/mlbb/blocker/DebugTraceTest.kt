package ru.mlbb.blocker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.mlbb.blocker.data.trace.DebugTrace
import ru.mlbb.blocker.data.trace.TraceEntry

class DebugTraceTest {

    private fun entry(
        phase: String = "QUEUE",
        category: String = "contact",
        ip: String = "146.185.199.1",
        port: Int = 5558,
        network: String = "tcp",
    ) = TraceEntry(
        ts = System.currentTimeMillis(),
        phase = phase,
        category = category,
        ip = ip,
        port = port,
        network = network,
    )

    @Test
    fun recordsAndClearsEntries() {
        DebugTrace.clear()
        DebugTrace.record(entry())
        DebugTrace.record(entry(ip = "92.223.80.21", category = "udp_probe", network = "udp"))
        assertEquals(2, DebugTrace.snapshot().size)

        DebugTrace.clear()
        assertTrue(DebugTrace.snapshot().isEmpty())
    }

    @Test
    fun capsRingBuffer() {
        DebugTrace.clear()
        repeat(DebugTrace.MAX_ENTRIES + 50) { index ->
            DebugTrace.record(entry(ip = "10.0.0.$index"))
        }
        assertEquals(DebugTrace.MAX_ENTRIES, DebugTrace.snapshot().size)
    }

    @Test
    fun exportIsJsonWithoutPayloadContents() {
        DebugTrace.clear()
        DebugTrace.record(entry())
        val json = DebugTrace.exportJson()
        assertTrue(json.contains("146.185.199.1"))
        assertTrue(json.contains("\"entries\""))
        assertFalse(json.contains("payload"))
    }

    @Test
    fun analysesQueueProbesByRegion() {
        val entries = listOf(
            entry(phase = "QUEUE", category = "udp_probe", ip = "146.185.199.1", network = "udp"),
            entry(phase = "QUEUE", category = "udp_probe", ip = "146.185.199.1", network = "udp"),
            entry(phase = "QUEUE", category = "tcp_handshake", ip = "92.223.80.21", network = "tcp"),
            entry(phase = "MATCH_FOUND", category = "contact", ip = "146.185.199.1"),
            entry(phase = "QUEUE", category = "contact", ip = "192.168.0.5"),
            entry(phase = "QUEUE", category = "udp_probe", ip = "203.0.113.9"),
        )
        val regionOf: (String) -> String? = { ip ->
            when {
                ip.startsWith("146.185.199") -> "NSK"
                ip.startsWith("92.223.80") -> "DV"
                else -> null
            }
        }

        val report = DebugTrace.analyseQueueProbes(entries, regionOf)

        assertTrue(report.hasPingProbes)
        assertEquals(5, report.queueContacts)
        assertEquals(listOf("NSK", "DV"), report.probes.map { it.region })
        assertEquals(2, report.probes.first { it.region == "NSK" }.count)
        assertTrue(report.summary.contains("NSK"))
    }

    @Test
    fun reportsNoProbesHonestly() {
        val report = DebugTrace.analyseQueueProbes(
            listOf(entry(phase = "QUEUE", category = "contact", ip = "8.8.8.8")),
            { null },
        )
        assertFalse(report.hasPingProbes)
        assertTrue(report.summary.contains("не подтверждается"))
    }
}
