package ru.mlbb.blocker.data

import ru.mlbb.blocker.data.rules.RuleConfigRepository

object StartupEndpoints {

    /**
     * Пул логина/матчмейкинга MLBB: тот же московский /24, что и матч-серверы.
     * Разрешаем его только на порту [LOGIN_PORT], иначе клиент не может войти,
     * когда сервер выдаёт .152/.155/.156/.157 вместо .151.
     */
    val LOGIN_POOL_CIDRS = listOf("154.85.91.0/24")

    val MAIN_IP: String
        get() = RuleConfigRepository.current.startup.firstOrNull()?.ip
            ?: ru.mlbb.blocker.data.rules.BuiltInRules.config.startup.first().ip

    val MAIN_PORT: Int
        get() = RuleConfigRepository.current.startup.firstOrNull()?.port
            ?: ru.mlbb.blocker.data.rules.BuiltInRules.config.startup.first().port

    val LOGIN_PORT: Int
        get() = MAIN_PORT

    val CIDRS: List<String>
        get() = RuleConfigRepository.current.startup.map { it.cidr }

    fun isMainConnection(ip: String, port: Int): Boolean {
        return RuleConfigRepository.current.startup.any { it.ip == ip && it.port == port }
    }
}
