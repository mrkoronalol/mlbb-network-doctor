package ru.mlbb.blocker.config

import org.json.JSONArray
import org.json.JSONObject

/**
 * Правила «не трогать» для служебных портов логина.
 *
 * Порт 30021/30071/30190/30220 у Moonton — это вход, матчмейкинг и пробы регионов.
 * Их нельзя ни блокировать, ни задерживать.
 *
 * В sing-box 1.14 такие правила безопаснее задавать action `route` + `outbound: direct`
 * (старый синтаксис), а не через `port`-правила без адреса: в проверке конфига они
 * могут считаться пустыми.
 */
object BypassRules {

    val SERVICE_PORTS: List<Int> = listOf(30021, 30071, 30190, 30220)

    fun append(routeRules: JSONArray) {
        routeRules.put(
            JSONObject()
                .put("port", JSONArray(SERVICE_PORTS))
                .put("action", "route")
                .put("outbound", "direct"),
        )
        // UDP-вариант отдельно: логин переключается на UDP после первого пакета.
        routeRules.put(
            JSONObject()
                .put("port", JSONArray(SERVICE_PORTS))
                .put("network", "udp")
                .put("action", "route")
                .put("outbound", "direct"),
        )
    }
}
