package ru.mlbb.blocker.data

/**
 * Выбор пинга для дашборда.
 *
 * Раньше дашборд брал «самое свежее» соединение на любом матч-порту, включая
 * далёкие сервисные пробы (30190), поэтому цифра скакала «как будто пинг вырос
 * на всех IP». Здесь фиксируем правило:
 * - только матч-порты (сервис 30190 и пр. исключены),
 * - только свежие записи (≤ [freshMs]),
 * - если свежих нет — последний известный замер, но только на матч-порту.
 */
object DashboardPing {

    const val FRESH_MS = 15_000L

    fun select(connections: List<ConnectionStat>, now: Long, freshMs: Long = FRESH_MS): Long? {
        val matches = connections.filter {
            GamePorts.isMatchPort(it.port) && it.pingMs != null && it.observations > 0
        }
        val fresh = matches.filter { now - it.lastSeen <= freshMs }
        val pool = fresh.ifEmpty { matches }
        return pool.maxByOrNull { it.lastSeen }?.pingMs
    }
}
