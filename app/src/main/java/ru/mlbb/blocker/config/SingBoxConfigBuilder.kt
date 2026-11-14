package ru.mlbb.blocker.config

import org.json.JSONArray
import org.json.JSONObject
import ru.mlbb.blocker.BuildConfig
import ru.mlbb.blocker.data.GamePorts

/**
 * Генератор конфигурации sing-box.
 *
 * Блокировка — route action `reject`:
 * - `method: "default"` — TCP RST + ICMP unreachable для UDP (быстрый отказ),
 * - `method: "drop"` — молчаливое отбрасывание.
 *
 * Задержка — outbound `socks` на локальный [DelayShaper] (127.0.0.1),
 * применяется только к UDP-трафику нежелательных регионов. Логин-пул и
 * сервисные порты идут раньше в правилах и в релей не попадают.
 */
object SingBoxConfigBuilder {

    const val MLBB_PACKAGE = "com.mobile.legends"
    const val MLBB_PACKAGE_XIAOMI = "com.mobilelegends.mi"
    val MLBB_PACKAGES = listOf(MLBB_PACKAGE, MLBB_PACKAGE_XIAOMI)
    const val TEST_PACKAGE = "com.android.shell"

    const val REJECT_RESET = "default"
    const val REJECT_DROP = "drop"

    fun build(
        rules: List<String>,
        profileRules: List<String> = emptyList(),
        allowRules: List<String> = emptyList(),
        loginRules: List<String> = emptyList(),
        loginPorts: List<Int> = emptyList(),
        blockMatchPorts: Boolean = false,
        discoveryMode: Boolean,
        clashPort: Int,
        clashSecret: String,
        mtu: Int = 1400,
        tunStack: String = "system",
        rejectMethod: String = REJECT_RESET,
        logOutput: String? = null,
        logLevel: String = "info",
        blocksEnabled: Boolean = true,
        delayEnabled: Boolean = true,
        delayCidrs: List<String> = emptyList(),
        delayRelayPort: Int = 0,
        delayLoginPool: Boolean = false,
        safePoolCidrs: List<String> = emptyList(),
    ): String {
        val includedPackages = MLBB_PACKAGES + if (BuildConfig.DEBUG) listOf(TEST_PACKAGE) else emptyList()
        val root = JSONObject()

        val log = JSONObject()
            .put("level", logLevel)
            .put("timestamp", true)
        if (logOutput != null) {
            log.put("output", logOutput)
        }
        root.put("log", log)

        root.put(
            "dns",
            JSONObject()
                .put(
                    "servers",
                    JSONArray().put(
                        JSONObject()
                            .put("type", "local")
                            .put("tag", "local"),
                    ),
                )
                .put("final", "local"),
        )

        val tun = JSONObject()
            .put("type", "tun")
            .put("tag", "tun-in")
            .put("address", JSONArray().put("172.19.0.1/30").put("fdfe:dcba:9876::1/126"))
            .put("mtu", mtu)
            .put("auto_route", true)
            .put("strict_route", false)
            .put("stack", tunStack)
            .put("include_package", JSONArray().also { array -> includedPackages.forEach(array::put) })

        root.put("inbounds", JSONArray().put(tun))

        val outbounds = JSONArray()
            .put(
                JSONObject()
                    .put("type", "direct")
                    .put("tag", "direct"),
            )
        if (delayEnabled && delayRelayPort > 0 && (delayCidrs.isNotEmpty() || delayLoginPool)) {
            outbounds.put(
                JSONObject()
                    .put("type", "socks")
                    .put("tag", "delay-relay")
                    .put("server", "127.0.0.1")
                    .put("server_port", delayRelayPort)
                    .put("version", "5"),
            )
        }
        root.put("outbounds", outbounds)

        fun rejectRule(): JSONObject = JSONObject()
            .put("action", "reject")
            .put("method", rejectMethod)

        val routeRules = JSONArray()
        routeRules.put(
            JSONObject()
                .put("ip_is_private", true)
                .put("action", "route")
                .put("outbound", "direct"),
        )
        if (allowRules.isNotEmpty()) {
            routeRules.put(
                JSONObject()
                    .put("ip_cidr", JSONArray(allowRules))
                    .put("action", "route")
                    .put("outbound", "direct"),
            )
        }
        // Explicit login-infra bypass: known login pools x service ports.
        // ip_cidr + port is more robust than a port-only rule: in sing-box 1.14
        // a rule with only a port may be treated as empty during config check.
        if (safePoolCidrs.isNotEmpty()) {
            routeRules.put(
                JSONObject()
                    .put("ip_cidr", JSONArray(safePoolCidrs))
                    .put("port", JSONArray(GamePorts.SAFE_PORT_LIST))
                    .put("action", "route")
                    .put("outbound", "direct"),
            )
        }
        // Fallback: login pool may be wider than hardcoded lists, these ports
        // are never touched for anyone.
        routeRules.put(
            JSONObject()
                .put("port", JSONArray(GamePorts.SAFE_PORT_LIST))
                .put("action", "route")
                .put("outbound", "direct"),
        )
        BypassRules.append(routeRules)
        if (loginRules.isNotEmpty() && loginPorts.isNotEmpty()) {
            routeRules.put(
                JSONObject()
                    .put("ip_cidr", JSONArray(loginRules))
                    .put("port", JSONArray(loginPorts))
                    .put("action", "route")
                    .put("outbound", if (delayLoginPool) "delay-relay" else "direct"),
            )
        }
        if (delayEnabled && delayCidrs.isNotEmpty() && delayRelayPort > 0) {
            routeRules.put(
                JSONObject()
                    .put("network", "udp")
                    .put("ip_cidr", JSONArray(delayCidrs))
                    .put("action", "route")
                    .put("outbound", "delay-relay"),
            )
        }
        if (!discoveryMode && blocksEnabled && rules.isNotEmpty()) {
            routeRules.put(
                rejectRule()
                    .put("ip_cidr", JSONArray(rules)),
            )
        }
        if (!discoveryMode && blocksEnabled && profileRules.isNotEmpty()) {
            val rangePorts = GamePorts.MATCH_PORT_RANGES
            val singlePorts = GamePorts.MATCH_PORT_LIST
            if (singlePorts.isNotEmpty()) {
                routeRules.put(
                    rejectRule()
                        .put("ip_cidr", JSONArray(profileRules))
                        .put("port", JSONArray(singlePorts)),
                )
            }
            if (rangePorts.isNotEmpty()) {
                routeRules.put(
                    rejectRule()
                        .put("ip_cidr", JSONArray(profileRules))
                        .put("port_range", JSONArray(rangePorts)),
                )
            }
        }
        if (!discoveryMode && blocksEnabled && blockMatchPorts) {
            val rangePorts = GamePorts.MATCH_PORT_RANGES
            val singlePorts = GamePorts.MATCH_PORT_LIST
            if (singlePorts.isNotEmpty()) {
                routeRules.put(
                    rejectRule()
                        .put("port", JSONArray(singlePorts)),
                )
            }
            if (rangePorts.isNotEmpty()) {
                routeRules.put(
                    rejectRule()
                        .put("port_range", JSONArray(rangePorts)),
                )
            }
        }

        root.put(
            "route",
            JSONObject()
                .put("auto_detect_interface", true)
                .put("default_domain_resolver", "local")
                .put("rules", routeRules)
                .put("final", "direct"),
        )

        root.put(
            "experimental",
            JSONObject().put(
                "clash_api",
                JSONObject()
                    .put("external_controller", "127.0.0.1:$clashPort")
                    .put("secret", clashSecret),
            ),
        )

        return root.toString(2)
    }
}
