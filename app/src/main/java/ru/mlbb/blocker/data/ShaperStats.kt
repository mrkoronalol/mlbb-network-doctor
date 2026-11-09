package ru.mlbb.blocker.data

/**
 * Метрики локального шейпера задержки (этап 2).
 */
data class ShaperStats(
    val packetsDelayed: Long = 0,
    val avgDelayMs: Long = 0,
    val activeFlows: Int = 0,
) {
    val summary: String
        get() = "пакетов задержано: $packetsDelayed · средняя задержка: $avgDelayMs мс · потоков: $activeFlows"
}
