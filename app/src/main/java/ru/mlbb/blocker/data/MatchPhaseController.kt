package ru.mlbb.blocker.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Фазовая машина очереди/матча.
 *
 * Переходы:
 * - IDLE -> QUEUE: замечен трафик логин-пула/сервисных портов.
 * - QUEUE -> MATCH_FOUND: match-порт увидел `onMatchFound` (оверлей «Матч найден»).
 * - MATCH_FOUND -> IN_GAME: пошёл трафик на матч-порт.
 * - * -> IDLE: трафик MLBB не наблюдался дольше `IDLE_AFTER_MS` (watchdog в сервисе).
 *
 * Правило фильтрации:
 * - IDLE/QUEUE — полная фильтрация.
 * - MATCH_FOUND — без блокировок и задержек (пусть подтверждение пройдёт).
 * - IN_GAME — только задержка, никогда блок (не рвать сессию → нет dodge).
 */
object MatchPhaseController {

    private const val IDLE_AFTER_MS = 90_000L

    private val _phase = MutableStateFlow(MatchPhase.IDLE)
    val phase: StateFlow<MatchPhase> = _phase.asStateFlow()

    @Volatile
    private var lastTrafficAt = 0L

    fun filterFor(phase: MatchPhase): PhaseFilter = when (phase) {
        MatchPhase.IDLE, MatchPhase.QUEUE -> PhaseFilter.ACTIVE
        MatchPhase.MATCH_FOUND -> PhaseFilter.BLOCKS_OFF
        MatchPhase.IN_GAME -> PhaseFilter.DELAY_ONLY
    }

    fun onLoginTraffic() {
        if (_phase.value == MatchPhase.IDLE) _phase.value = MatchPhase.QUEUE
        touch()
    }

    /** Явный старт очереди по кнопке «Начать поиск» — не зависит от трафика. */
    fun startQueue() {
        if (_phase.value == MatchPhase.IDLE || _phase.value == MatchPhase.IN_GAME) {
            _phase.value = MatchPhase.QUEUE
        }
        touch()
    }

    fun onMatchFound() {
        if (_phase.value == MatchPhase.QUEUE || _phase.value == MatchPhase.IDLE) {
            _phase.value = MatchPhase.MATCH_FOUND
        }
        touch()
    }

    fun onMatchTraffic() {
        if (_phase.value == MatchPhase.MATCH_FOUND) _phase.value = MatchPhase.IN_GAME
        touch()
    }

    fun onAnyTraffic() {
        touch()
    }

    fun onSessionEnd() {
        _phase.value = MatchPhase.IDLE
    }

    /** Watchdog: сбрасывает фазу, если MLBB-трафик давно не наблюдался. */
    fun resetIfIdle(now: Long = System.currentTimeMillis(), idleAfterMs: Long = IDLE_AFTER_MS) {
        if (_phase.value != MatchPhase.IDLE && now - lastTrafficAt > idleAfterMs) {
            _phase.value = MatchPhase.IDLE
        }
    }

    fun reset() {
        _phase.value = MatchPhase.IDLE
        lastTrafficAt = 0L
    }

    private fun touch() {
        lastTrafficAt = System.currentTimeMillis()
    }
}
