package ru.mlbb.blocker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.mlbb.blocker.data.AutoProfileAdvisor
import ru.mlbb.blocker.data.ConnectionStat
import ru.mlbb.blocker.data.RegionProfiles

class AutoProfileAdvisorTest {

    private fun connection(ip: String, pingMs: Long?) = ConnectionStat(
        ip = ip,
        port = 10003,
        host = null,
        network = "wifi",
        firstSeen = 0L,
        lastSeen = 0L,
        observations = 1,
        pingMs = pingMs,
    )

    @Test
    fun picksRegionWithLowestMeasuredPing() {
        val picked = AutoProfileAdvisor.pick(
            listOf(
                connection("92.223.80.21", 60),
                connection("154.85.91.157", 20),
            ),
        )
        assertEquals(RegionProfiles.ID_MOSCOW, picked)
    }

    @Test
    fun prefersFarEastWhenItIsFaster() {
        val picked = AutoProfileAdvisor.pick(
            listOf(
                connection("92.223.80.21", 35),
                connection("154.85.91.157", 90),
                connection("146.185.199.1", 70),
            ),
        )
        assertEquals(RegionProfiles.ID_FAR_EAST, picked)
    }

    @Test
    fun returnsNullWithoutPings() {
        assertNull(
            AutoProfileAdvisor.pick(
                listOf(
                    connection("92.223.80.21", null),
                    connection("203.0.113.10", 10),
                ),
            ),
        )
    }
}
