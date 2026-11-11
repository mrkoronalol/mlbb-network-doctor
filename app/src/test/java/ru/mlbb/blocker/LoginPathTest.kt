package ru.mlbb.blocker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.mlbb.blocker.config.SingBoxConfigBuilder
import org.json.JSONObject

/**
 * Регрессия входа в игру (ноябрь 2026): режим задержки не должен ломать логин,
 * а прямой профиль должен оставлять логин-пул быстрым.
 */
class LoginPathTest {

    private fun build(
        delayCidrs: List<String> = emptyList(),
        delayRelayPort: Int = 0,
        delayLoginPool: Boolean = false,
        blocksEnabled: Boolean = true,
    ): JSONObject = JSONObject(
        SingBoxConfigBuilder.build(
            rules = emptyList(),
            profileRules = emptyList(),
            allowRules = listOf("92.223.80.0/24", "5.188.3.0/24"),
            loginRules = listOf("154.85.91.0/24"),
            loginPorts = listOf(10003),
            blockMatchPorts = true,
            discoveryMode = false,
            clashPort = 39090,
            clashSecret = "secret",
            blocksEnabled = blocksEnabled,
            delayCidrs = delayCidrs,
            delayRelayPort = delayRelayPort,
            delayLoginPool = delayLoginPool,
        ),
    )

    private fun loginRule(config: JSONObject): JSONObject {
        val rules = config.getJSONObject("route").getJSONArray("rules")
        for (index in 0 until rules.length()) {
            val rule = rules.getJSONObject(index)
            if (rule.has("ip_cidr") && rule.has("port") &&
                rule.getJSONArray("port").getInt(0) == 10003
            ) {
                return rule
            }
        }
        throw AssertionError("login rule not found")
    }

    @Test
    fun defaultConfigKeepsLoginPoolDirect() {
        val config = build()
        val rule = loginRule(config)
        assertEquals("direct", rule.getString("outbound"))
    }

    @Test
    fun delayModeRoutesLoginPoolThroughRelay() {
        val config = build(
            delayCidrs = listOf("146.185.199.0/24", "92.223.91.0/24"),
            delayRelayPort = 41000,
            delayLoginPool = true,
        )
        val rule = loginRule(config)
        assertEquals("delay-relay", rule.getString("outbound"))

        // Аутбаунд релея существует и указывает на локальный SOCKS5.
        val outbounds = config.getJSONArray("outbounds")
        var found = false
        for (index in 0 until outbounds.length()) {
            val outbound = outbounds.getJSONObject(index)
            if (outbound.optString("tag") == "delay-relay") {
                found = true
                assertEquals("127.0.0.1", outbound.getString("server"))
                assertEquals(41000, outbound.getInt("server_port"))
            }
        }
        assertTrue(found)
    }

    @Test
    fun farEastAllowListStaysDirectEvenInDelayMode() {
        val config = build(
            delayCidrs = listOf("146.185.199.0/24"),
            delayRelayPort = 41000,
            delayLoginPool = true,
        )
        val rules = config.getJSONObject("route").getJSONArray("rules")
        val allowIndex = (0 until rules.length()).first { index ->
            val rule = rules.getJSONObject(index)
            rule.has("ip_cidr") && rule.optString("outbound") == "direct" &&
                rule.getJSONArray("ip_cidr").getString(0) == "92.223.80.0/24"
        }
        val loginIndex = (0 until rules.length()).first { index ->
            val rule = rules.getJSONObject(index)
            rule.has("ip_cidr") && rule.has("port") && rule.optString("outbound") == "delay-relay"
        }
        assertTrue("allow rule must come before login delay", allowIndex < loginIndex)
    }
}
