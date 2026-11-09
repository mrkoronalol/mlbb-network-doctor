package ru.mlbb.blocker

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.mlbb.blocker.config.SingBoxConfigBuilder

class SingBoxConfigBuilderTest {

    private fun build(
        rules: List<String> = emptyList(),
        profileRules: List<String> = emptyList(),
        allowRules: List<String> = emptyList(),
        loginRules: List<String> = emptyList(),
        loginPorts: List<Int> = emptyList(),
        blockMatchPorts: Boolean = false,
        discoveryMode: Boolean = false,
        rejectMethod: String = SingBoxConfigBuilder.REJECT_RESET,
        blocksEnabled: Boolean = true,
        delayEnabled: Boolean = true,
        delayCidrs: List<String> = emptyList(),
        delayRelayPort: Int = 0,
    ): JSONObject = JSONObject(
        SingBoxConfigBuilder.build(
            rules = rules,
            profileRules = profileRules,
            allowRules = allowRules,
            loginRules = loginRules,
            loginPorts = loginPorts,
            blockMatchPorts = blockMatchPorts,
            discoveryMode = discoveryMode,
            clashPort = 39090,
            clashSecret = "test-secret",
            rejectMethod = rejectMethod,
            blocksEnabled = blocksEnabled,
            delayEnabled = delayEnabled,
            delayCidrs = delayCidrs,
            delayRelayPort = delayRelayPort,
        ),
    )

    @Test
    fun blockingConfigRejectsBlockedSubnets() {
        val config = build(
            rules = listOf("154.85.91.0/24", "66.253.15.83/32"),
        )

        val route = config.getJSONObject("route")
        assertEquals("direct", route.getString("final"))
        assertEquals(true, route.getBoolean("auto_detect_interface"))

        val rules = route.getJSONArray("rules")
        var rejectFound = false
        for (index in 0 until rules.length()) {
            val rule = rules.getJSONObject(index)
            if (rule.optString("action") == "reject") {
                rejectFound = true
                assertEquals(2, rule.getJSONArray("ip_cidr").length())
                assertEquals(SingBoxConfigBuilder.REJECT_RESET, rule.optString("method"))
            }
        }
        assertTrue(rejectFound)

        // Sink-аутбаунда больше нет: блокировка — route action reject.
        val outbounds = config.getJSONArray("outbounds")
        for (index in 0 until outbounds.length()) {
            assertFalse(outbounds.getJSONObject(index).optString("tag") == "sink")
        }
    }

    @Test
    fun rejectSupportsDropMethod() {
        val config = build(
            rules = listOf("154.85.91.0/24"),
            rejectMethod = SingBoxConfigBuilder.REJECT_DROP,
        )
        val rules = config.getJSONObject("route").getJSONArray("rules")
        var method: String? = null
        for (index in 0 until rules.length()) {
            val rule = rules.getJSONObject(index)
            if (rule.optString("action") == "reject") method = rule.optString("method")
        }
        assertEquals(SingBoxConfigBuilder.REJECT_DROP, method)
    }

    @Test
    fun allowRulesComeBeforeRejectRules() {
        val config = build(
            rules = listOf("154.85.91.0/24"),
            allowRules = listOf("154.85.91.153/32"),
        )
        val rules = config.getJSONObject("route").getJSONArray("rules")
        var allowIndex = -1
        var blockIndex = -1
        for (index in 0 until rules.length()) {
            val rule = rules.getJSONObject(index)
            if (rule.optString("action") == "route" && rule.has("ip_cidr")) {
                allowIndex = index
            }
            if (rule.optString("action") == "reject") {
                blockIndex = index
            }
        }
        assertTrue(allowIndex >= 0)
        assertTrue(blockIndex >= 0)
        assertTrue(allowIndex < blockIndex)
    }

    @Test
    fun discoveryModeOmitsRejectRules() {
        val config = build(
            rules = listOf("154.85.91.0/24"),
            discoveryMode = true,
        )
        val rules = config.getJSONObject("route").getJSONArray("rules")
        for (index in 0 until rules.length()) {
            assertFalse(rules.getJSONObject(index).optString("action") == "reject")
        }
    }

    @Test
    fun allowOnlyProfileRejectsOtherMatchPortsAfterAllowRule() {
        val config = build(
            rules = listOf("146.185.199.0/24"),
            allowRules = listOf("154.85.91.0/24"),
            blockMatchPorts = true,
        )
        val rules = config.getJSONObject("route").getJSONArray("rules")
        var allowIndex = -1
        var portBlockIndex = -1
        var rangeBlockIndex = -1
        for (index in 0 until rules.length()) {
            val rule = rules.getJSONObject(index)
            if (rule.optString("action") == "route" && rule.has("ip_cidr")) allowIndex = index
            if (rule.optString("action") == "reject" && rule.has("port")) portBlockIndex = index
            if (rule.optString("action") == "reject" && rule.has("port_range")) rangeBlockIndex = index
        }
        assertTrue(allowIndex >= 0)
        assertTrue(portBlockIndex > allowIndex)
        assertTrue(rangeBlockIndex > allowIndex)
        assertEquals(10003, rules.getJSONObject(portBlockIndex).getJSONArray("port").getInt(0))
        assertEquals("5000:5058", rules.getJSONObject(rangeBlockIndex).getJSONArray("port_range").getString(0))
    }

    @Test
    fun profileRulesRejectOnlyMatchPorts() {
        val config = build(
            profileRules = listOf("154.85.91.0/24", "146.185.199.0/24"),
        )
        val rules = config.getJSONObject("route").getJSONArray("rules")
        var withPort = 0
        var withRange = 0
        for (index in 0 until rules.length()) {
            val rule = rules.getJSONObject(index)
            if (rule.optString("action") != "reject" || !rule.has("ip_cidr")) continue
            when {
                rule.has("port") -> withPort++
                rule.has("port_range") -> withRange++
                else -> throw AssertionError("Profile CIDR is rejected on all ports")
            }
        }
        assertEquals(1, withPort)
        assertEquals(1, withRange)
    }

    @Test
    fun gameProbePort30190AlwaysGoesDirect() {
        val config = build(
            rules = listOf("139.45.245.2/32", "92.223.91.0/24"),
        )
        val rules = config.getJSONObject("route").getJSONArray("rules")
        var probeIndex = -1
        var blockIndex = -1
        for (index in 0 until rules.length()) {
            val rule = rules.getJSONObject(index)
            if (rule.optString("action") == "route" && rule.has("port")) {
                probeIndex = index
            }
            if (rule.optString("action") == "reject" && rule.has("ip_cidr")) {
                blockIndex = index
            }
        }
        assertTrue(probeIndex >= 0)
        assertTrue(blockIndex > probeIndex)
        val safePorts = rules.getJSONObject(probeIndex).getJSONArray("port")
        val values = (0 until safePorts.length()).map { safePorts.getInt(it) }
        assertTrue(30190 in values)
    }

    @Test
    fun discoveryModeOmitsMatchPortReject() {
        val config = build(
            allowRules = listOf("154.85.91.0/24"),
            blockMatchPorts = true,
            discoveryMode = true,
        )
        val rules = config.getJSONObject("route").getJSONArray("rules")
        for (index in 0 until rules.length()) {
            assertFalse(rules.getJSONObject(index).optString("action") == "reject")
        }
    }

    @Test
    fun loginPoolIsAllowedOnMatchmakingPortBeforeReject() {
        val config = build(
            rules = listOf("154.85.91.0/24"),
            profileRules = listOf("154.85.91.0/24"),
            loginRules = listOf("154.85.91.0/24"),
            loginPorts = listOf(10003),
            blockMatchPorts = true,
        )
        val rules = config.getJSONObject("route").getJSONArray("rules")
        var loginIndex = -1
        var userBlockIndex = -1
        var catchAllIndex = -1
        for (index in 0 until rules.length()) {
            val rule = rules.getJSONObject(index)
            val action = rule.optString("action")
            if (action == "route" && rule.has("ip_cidr") && rule.has("port")) {
                loginIndex = index
            }
            if (action == "reject" && rule.has("ip_cidr") && !rule.has("port") && !rule.has("port_range")) {
                userBlockIndex = index
            }
            if (action == "reject" && !rule.has("ip_cidr") && rule.has("port")) {
                catchAllIndex = index
            }
        }
        assertTrue(loginIndex >= 0)
        assertTrue(userBlockIndex > loginIndex)
        assertTrue(catchAllIndex > loginIndex)
        assertEquals(10003, rules.getJSONObject(loginIndex).getJSONArray("port").getInt(0))
    }

    @Test
    fun phaseWithBlocksOffOmitsRejectAndDelay() {
        val config = build(
            rules = listOf("154.85.91.0/24"),
            blockMatchPorts = true,
            delayCidrs = listOf("146.185.199.0/24"),
            delayRelayPort = 41000,
            blocksEnabled = false,
            delayEnabled = false,
        )
        val rules = config.getJSONObject("route").getJSONArray("rules")
        for (index in 0 until rules.length()) {
            val rule = rules.getJSONObject(index)
            assertFalse(rule.optString("action") == "reject")
            assertFalse(rule.optString("outbound") == "delay-relay")
        }
        val outbounds = config.getJSONArray("outbounds")
        for (index in 0 until outbounds.length()) {
            assertFalse(outbounds.getJSONObject(index).optString("tag") == "delay-relay")
        }
    }

    @Test
    fun delayRelayRoutesOnlyUdpToLocalSocks() {
        val config = build(
            blockMatchPorts = true,
            delayCidrs = listOf("146.185.199.0/24", "92.223.91.0/24"),
            delayRelayPort = 41000,
        )
        val outbounds = config.getJSONArray("outbounds")
        var relayFound = false
        for (index in 0 until outbounds.length()) {
            val outbound = outbounds.getJSONObject(index)
            if (outbound.optString("tag") == "delay-relay") {
                relayFound = true
                assertEquals("socks", outbound.getString("type"))
                assertEquals("127.0.0.1", outbound.getString("server"))
                assertEquals(41000, outbound.getInt("server_port"))
            }
        }
        assertTrue(relayFound)

        val rules = config.getJSONObject("route").getJSONArray("rules")
        var delayRuleFound = false
        for (index in 0 until rules.length()) {
            val rule = rules.getJSONObject(index)
            if (rule.optString("outbound") == "delay-relay") {
                delayRuleFound = true
                assertEquals("udp", rule.getString("network"))
                assertEquals(2, rule.getJSONArray("ip_cidr").length())
            }
        }
        assertTrue(delayRuleFound)
    }
}
