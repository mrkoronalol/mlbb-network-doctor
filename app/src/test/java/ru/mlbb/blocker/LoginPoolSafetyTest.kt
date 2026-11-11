package ru.mlbb.blocker

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.mlbb.blocker.config.SingBoxConfigBuilder
import ru.mlbb.blocker.data.rules.BuiltInRules

/**
 * Регрессия: профиль «ДВ» не должен резать логин и матчмейкинг.
 * Раньше 154.85.91.0/24 был в блок-листе профиля и игра застревала на загрузке.
 */
class LoginPoolSafetyTest {

    private fun build(profileRules: List<String>, allowRules: List<String>): JSONObject {
        val loginPools = (ru.mlbb.blocker.data.StartupEndpoints.LOGIN_POOL_CIDRS +
            BuiltInRules.LOGIN_SERVICE_POOLS).distinct()
        return JSONObject(
            SingBoxConfigBuilder.build(
                rules = emptyList(),
                profileRules = profileRules,
                allowRules = allowRules + loginPools,
                loginRules = ru.mlbb.blocker.data.StartupEndpoints.LOGIN_POOL_CIDRS,
                loginPorts = listOf(10003),
                blockMatchPorts = true,
                discoveryMode = false,
                clashPort = 39090,
                clashSecret = "secret",
            ),
        )
    }

    @Test
    fun farEastProfileDoesNotBlockMoscowLoginPool() {
        val profileRules = BuiltInRules.FAR_EAST.rules.map { it.first }
        assertFalse("154.85.91.0/24 must not be a profile block rule", "154.85.91.0/24" in profileRules)

        val config = build(profileRules, BuiltInRules.FAR_EAST.allowedCidrs)
        val rules = config.getJSONObject("route").getJSONArray("rules")

        var loginDirectIndex = -1
        var firstRejectIndex = -1
        for (index in 0 until rules.length()) {
            val rule = rules.getJSONObject(index)
            if (rule.optString("action") == "route" &&
                rule.optString("outbound") == "direct" &&
                rule.has("ip_cidr") &&
                rule.getJSONArray("ip_cidr").toString().contains("154.85.91.0/24")
            ) {
                loginDirectIndex = index
            }
            if (rule.optString("action") == "reject" && firstRejectIndex < 0) {
                firstRejectIndex = index
            }
        }
        assertTrue("login pool must be routed direct", loginDirectIndex >= 0)
        if (firstRejectIndex >= 0) {
            assertTrue(
                "login direct must come before any reject",
                loginDirectIndex < firstRejectIndex,
            )
        }
    }

    @Test
    fun servicePoolsAreAlwaysDirect() {
        val config = build(BuiltInRules.FAR_EAST.rules.map { it.first }, BuiltInRules.FAR_EAST.allowedCidrs)
        val allowCidrs = config.getJSONObject("route").getJSONArray("rules")
            .let { rules ->
                (0 until rules.length()).map { rules.getJSONObject(it) }
                    .first { it.optString("action") == "route" && it.has("port") == false && it.has("ip_cidr") }
            }
            .getJSONArray("ip_cidr")
        val values = (0 until allowCidrs.length()).map { allowCidrs.getString(it) }

        for (pool in BuiltInRules.LOGIN_SERVICE_POOLS) {
            assertTrue("$pool must be allowed", pool in values)
        }
        assertEquals(true, "92.223.80.0/24" in values)
    }
}
