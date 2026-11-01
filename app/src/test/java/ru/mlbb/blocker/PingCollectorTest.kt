package ru.mlbb.blocker

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.mlbb.blocker.data.GamePorts
import ru.mlbb.blocker.data.PingCollector
import ru.mlbb.blocker.data.RegionProfiles
import ru.mlbb.blocker.data.RouteAction
import ru.mlbb.blocker.data.RuleEngine
import ru.mlbb.blocker.data.StartupEndpoints

class PingCollectorTest {

    @Test
    fun farEastPolicyAllowsLoginPoolOnMatchmakingPort() {
        val policy = PingCollector.policyFor(RegionProfiles.FAR_EAST, allowLoginPool = true)

        assertEquals(
            RouteAction.DIRECT,
            RuleEngine.decide("154.85.91.155", StartupEndpoints.LOGIN_PORT, policy).action,
        )
        assertEquals(
            RouteAction.DIRECT,
            RuleEngine.decide("92.223.80.21", 5501, policy).action,
        )
        assertEquals(
            RouteAction.SINK,
            RuleEngine.decide("154.85.91.155", 5558, policy).action,
        )
        assertEquals(
            RouteAction.SINK,
            RuleEngine.decide("92.223.91.5", 5558, policy).action,
        )
    }

    @Test
    fun loginPoolCanBeDisabled() {
        val policy = PingCollector.policyFor(RegionProfiles.FAR_EAST, allowLoginPool = false)

        assertEquals(
            RouteAction.SINK,
            RuleEngine.decide("154.85.91.155", StartupEndpoints.LOGIN_PORT, policy).action,
        )
    }

    @Test
    fun customProfileBlocksOnlyUserRules() {
        val policy = PingCollector.policyFor(RegionProfiles.CUSTOM, allowLoginPool = false)

        assertEquals(
            RouteAction.DIRECT,
            RuleEngine.decide("203.0.113.10", GamePorts.MATCH_PORT, policy).action,
        )
    }
}
