package ru.mlbb.blocker.data

/**
 * Фазы жизненного цикла матча. Управляет [MatchPhaseController].
 */
enum class MatchPhase {
    IDLE,
    QUEUE,
    MATCH_FOUND,
    IN_GAME,
}

/**
 * Как фильтровать трафик в фазе:
 * - ACTIVE — полная фильтрация (блок и/или задержка по настройкам).
 * - BLOCKS_OFF — ничего не блокировать и не задерживать (подтверждение матча).
 * - DELAY_ONLY — только задержка, блок запрещён (не рвать сессию).
 */
enum class PhaseFilter {
    ACTIVE,
    BLOCKS_OFF,
    DELAY_ONLY,
}
