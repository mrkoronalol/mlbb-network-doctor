package ru.mlbb.blocker

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.mlbb.blocker.data.ConnectionStat
import ru.mlbb.blocker.data.DashboardPing

/**
 * Регрессия: цифра «Пинг» на дашборде не должна показывать пинг случайного
 * сервера. Берём только игровые матч-порты и только реально свежие замеры.
 */
class DashboardPingSelectionTest {

    private fun conn(
        ip: String,
        port: Int,
        ping: Long?,
        lastSeen: Long = 1_000L,
        observations: Int = 1,
    ) = ConnectionStat(
        ip = ip,
        port = port,
        host = null,
        network = "udp",
        firstSeen = lastSeen,
        lastSeen = lastSeen,
        observations = observations,
        pingMs = ping,
    )

    @Test
    fun ignoresServicePortsAndStaleServers() {
        val now = 10_000L
        val connections = listOf(
            // Сервисный порт (Тбилиси-проба) — не «матч», не должен побеждать.
            conn("139.45.245.2", 30190, 400, lastSeen = now),
            // Устаревшее соединение, хотя пинг меньше.
            conn("92.223.80.21", 5501, 50, lastSeen = now - 60_000),
            // Актуальный Хабаровск.
            conn("5.188.3.161", 5551, 57, lastSeen = now - 1_000),
        )

        val selected = DashboardPing.select(connections, now)

        assertEquals(57L, selected)
    }

    @Test
    fun ignoresServersWithoutPing() {
        val now = 10_000L
        val selected = DashboardPing.select(
            listOf(
                conn("5.188.3.161", 5551, null, lastSeen = now),
                conn("92.223.80.21", 5501, 56, lastSeen = now - 2_000),
            ),
            now,
        )
        assertEquals(56L, selected)
    }

    @Test
    fun fallsBackToLastKnownMatchMeasurement() {
        val now = 10_000L
        val selected = DashboardPing.select(
            listOf(conn("5.188.3.161", 5551, 57, lastSeen = now - 120_000)),
            now,
        )
        assertEquals(57L, selected)
    }

    @Test
    fun returnsNullWhenNoMatchMeasurements() {
        val now = 10_000L
        val selected = DashboardPing.select(
            listOf(conn("139.45.245.2", 30190, 400, lastSeen = now)),
            now,
        )
        assertEquals(null, selected)
    }
}
