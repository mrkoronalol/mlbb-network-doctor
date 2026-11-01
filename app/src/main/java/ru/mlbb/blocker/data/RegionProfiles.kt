package ru.mlbb.blocker.data

import ru.mlbb.blocker.data.rules.BuiltInRules
import ru.mlbb.blocker.data.rules.RuleConfigRepository
import ru.mlbb.blocker.data.remote.RemoteConfigRepository

data class RegionProfile(
    val id: String,
    val title: String,
    val badge: String,
    val description: String,
    val rules: List<Pair<String, String>>,
    val allowedCidrs: List<String> = emptyList(),
    val blockOtherMatchServers: Boolean = false,
)

object RegionProfiles {

    const val ID_FAR_EAST = "far_east"
    const val ID_MOSCOW = "moscow"
    const val ID_NOVOSIBIRSK = "novosibirsk"
    const val ID_CUSTOM = "custom"

    /** Пользовательский профиль: работают только ручные правила блокировки. */
    val CUSTOM = RegionProfile(
        id = ID_CUSTOM,
        title = "Пользовательский",
        badge = "СВОЙ",
        description = "Только ваши правила из блок-листа. Профильные ограничения отключены.",
        rules = emptyList(),
        allowedCidrs = emptyList(),
        blockOtherMatchServers = false,
    )

    /** Пресеты для блоклиста остаются встроенными: это удобные заготовки, а не правила маршрутизации. */
    val MOSCOW_SERVERS: List<Pair<String, String>> get() = BuiltInRules.MOSCOW_SERVERS
    val NOVOSIBIRSK_SERVERS: List<Pair<String, String>> get() = BuiltInRules.NOVOSIBIRSK_SERVERS

    val all: List<RegionProfile>
        get() = RemoteConfigRepository.profiles.ifEmpty {
            RuleConfigRepository.current.profiles
        }

    /** Профили для выпадающего списка: авто, встроенные/удалённые и пользовательский. */
    val selectable: List<RegionProfile>
        get() = all + CUSTOM

    fun byId(id: String): RegionProfile? {
        if (id == ID_CUSTOM) return CUSTOM
        return all.firstOrNull { it.id == id }
    }

    val MOSCOW: RegionProfile
        get() = byId(ID_MOSCOW) ?: BuiltInRules.MOSCOW

    val FAR_EAST: RegionProfile
        get() = byId(ID_FAR_EAST) ?: BuiltInRules.FAR_EAST

    val NOVOSIBIRSK: RegionProfile
        get() = byId(ID_NOVOSIBIRSK) ?: BuiltInRules.NOVOSIBIRSK
}

/**
 * Выбирает профиль по фактическим замерам: сравнивает средний пинг подключений,
 * попадающих в allow-сети профиля. Если данных нет — профиль не меняется.
 */
object AutoProfileAdvisor {

    fun pick(connections: List<ConnectionStat>): String? {
        val scored = listOf(
            RegionProfiles.ID_MOSCOW,
            RegionProfiles.ID_FAR_EAST,
            RegionProfiles.ID_NOVOSIBIRSK,
        ).mapNotNull { id ->
            val profile = RegionProfiles.byId(id) ?: return@mapNotNull null
            val pings = connections
                .filter { connection ->
                    profile.allowedCidrs.any { Cidr.contains(it, connection.ip) }
                }
                .mapNotNull { it.pingMs }
            if (pings.isEmpty()) null else id to pings.average()
        }
        return scored.minByOrNull { it.second }?.first
    }
}
