package ru.mlbb.blocker.data.trace

import org.json.JSONArray
import org.json.JSONObject

/** Одна запись debug-trace: только метаданные, содержимое пакетов не сохраняется. */
data class TraceEntry(
    val ts: Long,
    val phase: String,
    val category: String, // contact | udp_probe | tcp_handshake | block | delay | phase
    val ip: String,
    val port: Int,
    val network: String,
    val uploadBytes: Long = 0L,
    val downloadBytes: Long = 0L,
)

data class RegionProbe(
    val region: String,
    val ip: String,
    val port: Int,
    val network: String,
    val count: Int,
)

data class ProbeReport(
    val queueContacts: Int,
    val probes: List<RegionProbe>,
    val hasPingProbes: Boolean,
) {
    val summary: String
        get() = if (queueContacts == 0) {
            "В очереди контактов не зафиксировано — начните поиск матча при включённом Debug trace."
        } else if (probes.isEmpty()) {
            "Очередь: $queueContacts контактов, но проб к регионам не обнаружено. " +
                "Гипотеза «игра ранжирует по пингу до назначения» не подтверждается."
        } else {
            "Очередь: $queueContacts контактов, пробы к регионам: " +
                probes.joinToString(", ") { "${it.region}×${it.count}" } +
                ". Гипотеза поддерживается — шейпер имеет смысл."
        }
}

/**
 * Кольцевой буфер диагностики MLBB-трафика (этап 0).
 * Включается Настройки → Для разработчиков → «Debug trace».
 */
object DebugTrace {

    const val MAX_ENTRIES = 4000

    private val entries = ArrayDeque<TraceEntry>()

    @Synchronized
    fun record(entry: TraceEntry) {
        entries.addLast(entry)
        while (entries.size > MAX_ENTRIES) entries.removeFirst()
    }

    @Synchronized
    fun snapshot(): List<TraceEntry> = entries.toList()

    @Synchronized
    fun clear() {
        entries.clear()
    }

    fun exportJson(): String {
        val array = JSONArray()
        for (entry in snapshot()) {
            array.put(
                JSONObject()
                    .put("ts", entry.ts)
                    .put("phase", entry.phase)
                    .put("category", entry.category)
                    .put("ip", entry.ip)
                    .put("port", entry.port)
                    .put("network", entry.network)
                    .put("uploadBytes", entry.uploadBytes)
                    .put("downloadBytes", entry.downloadBytes),
            )
        }
        return JSONObject().put("version", 1).put("entries", array).toString(2)
    }

    /**
     * Чистый анализ проб в фазе QUEUE. `regionOf` сопоставляет IP с регионом
     * (KnownServers/GeoIP); null — не считаем пробой к региону.
     */
    fun analyseQueueProbes(
        entries: List<TraceEntry>,
        regionOf: (String) -> String?,
    ): ProbeReport {
        val queue = entries.filter { it.phase == "QUEUE" }
        val probes = queue
            .filter {
                (it.category == "udp_probe" || it.category == "tcp_handshake" || it.category == "contact") &&
                    !Cidr.isPrivateCompat(it.ip)
            }
            .groupBy { it.ip }
            .mapNotNull { (ip, list) ->
                val region = regionOf(ip) ?: return@mapNotNull null
                RegionProbe(
                    region = region,
                    ip = ip,
                    port = list.first().port,
                    network = list.first().network,
                    count = list.size,
                )
            }
            .sortedByDescending { it.count }
        return ProbeReport(
            queueContacts = queue.size,
            probes = probes,
            hasPingProbes = probes.isNotEmpty(),
        )
    }
}

/** Локальный аналог Cidr-проверки без зависимостей Android. */
private object Cidr {
    fun isPrivateCompat(ip: String): Boolean {
        if (!ip.contains('.') || ip.contains(':')) return ip.contains(':')
        val first = ip.substringBefore('.').toIntOrNull() ?: return false
        return first == 10 || first == 127 || (first == 192 && ip.substringAfter('.').startsWith("168.")) ||
            (first == 172 && ip.substringAfter('.').substringBefore('.').toIntOrNull() in 16..31)
    }
}
