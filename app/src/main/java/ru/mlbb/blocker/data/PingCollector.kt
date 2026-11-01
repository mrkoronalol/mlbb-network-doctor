package ru.mlbb.blocker.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Одна цель для диагностического замера. */
data class PingTarget(
    val ip: String,
    val port: Int,
    val region: String,
)

data class PingTargetResult(
    val target: PingTarget,
    val pingMs: Long?,
    val blocked: Boolean,
)

data class PingReport(
    val fastest: List<PingTargetResult>,
    val blocked: List<PingTargetResult>,
)

/**
 * Пингует известные матч-серверы, делит их на «быстрые» и «заблокированные
 * текущим профилем». Результат используется на экране диагностики и уходит
 * анонимно на сайт.
 */
object PingCollector {

    val TARGETS: List<PingTarget> = listOf(
        PingTarget("92.223.80.21", 5501, "DV"),
        PingTarget("92.223.80.65", 5501, "DV"),
        PingTarget("154.85.91.157", 10003, "MSK"),
        PingTarget("154.85.91.155", 10003, "MSK"),
        PingTarget("92.223.91.5", 5558, "EKB"),
        PingTarget("146.185.197.79", 5551, "ROSTOV"),
        PingTarget("146.185.199.1", 5558, "NSK"),
        PingTarget("148.153.82.69", 10003, "EU"),
    )

    suspend fun collect(policy: BlockerPolicy): PingReport = coroutineScope {
        val results = TARGETS.map { target ->
            async(Dispatchers.IO) {
                val summary = NetworkPing.summarize(
                    ip = target.ip,
                    port = target.port,
                    timeoutMs = 1500,
                    attempts = 2,
                )
                PingTargetResult(
                    target = target,
                    pingMs = summary.avgMs,
                    blocked = RuleEngine.decide(target.ip, target.port, policy).action ==
                        RouteAction.SINK,
                )
            }
        }.awaitAll()

        val fastest = results
            .filter { !it.blocked && it.pingMs != null }
            .sortedBy { it.pingMs }
        val blocked = results
            .filter { it.blocked }
            .sortedBy { it.pingMs ?: Long.MAX_VALUE }
        PingReport(fastest = fastest, blocked = blocked)
    }

    fun policyFor(profile: RegionProfile?, allowLoginPool: Boolean = true): BlockerPolicy =
        BlockerPolicy(
            allowCidrs = profile?.allowedCidrs.orEmpty(),
            profileBlockCidrs = profile?.rules?.map { it.first }.orEmpty(),
            blockOtherMatchPorts = profile?.blockOtherMatchServers == true,
            loginCidrs = if (allowLoginPool) StartupEndpoints.LOGIN_POOL_CIDRS else emptyList(),
            loginPorts = if (allowLoginPool) setOf(StartupEndpoints.LOGIN_PORT) else emptySet(),
        )
}
