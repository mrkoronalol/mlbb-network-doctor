package ru.mlbb.blocker.data

import ru.mlbb.blocker.data.geo.GeoIpRepository

/**
 * Локация сервера: сначала проверенный список из правил, затем GeoIP.
 * В логах и карточках раньше показывалось только GeoIP, поэтому московские
 * подсети подписывались чужими городами.
 */
object ServerLocation {

    fun label(ip: String): String? {
        val known = KnownServers.lookup(ip)?.city?.takeIf { it.isNotBlank() }
        if (known != null) return known
        val geo = runCatching { GeoIpRepository.lookup(ip) }.getOrNull() ?: return null
        return geo.city?.takeIf { it.isNotBlank() } ?: geo.country?.takeIf { it.isNotBlank() }
    }
}
