package ru.mlbb.blocker

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.mlbb.blocker.data.MatchPhase
import ru.mlbb.blocker.data.MatchPhaseController
import ru.mlbb.blocker.data.PhaseFilter

class MatchPhaseControllerTest {

    @Test
    fun startsIdleAndEntersQueueOnLoginTraffic() {
        MatchPhaseController.reset()
        assertEquals(MatchPhase.IDLE, MatchPhaseController.phase.value)

        MatchPhaseController.onLoginTraffic()
        assertEquals(MatchPhase.QUEUE, MatchPhaseController.phase.value)

        MatchPhaseController.onLoginTraffic()
        assertEquals(MatchPhase.QUEUE, MatchPhaseController.phase.value)
    }

    @Test
    fun matchFoundTransitionsToInGameOnMatchTraffic() {
        MatchPhaseController.reset()
        MatchPhaseController.onLoginTraffic()
        MatchPhaseController.onMatchFound()
        assertEquals(MatchPhase.MATCH_FOUND, MatchPhaseController.phase.value)

        MatchPhaseController.onMatchTraffic()
        assertEquals(MatchPhase.IN_GAME, MatchPhaseController.phase.value)

        // Повторный match-контакт не сбрасывает IN_GAME в MATCH_FOUND.
        MatchPhaseController.onMatchFound()
        assertEquals(MatchPhase.IN_GAME, MatchPhaseController.phase.value)
    }

    @Test
    fun matchTrafficWithoutMatchFoundDoesNothing() {        MatchPhaseController.reset()
        MatchPhaseController.onMatchTraffic()
        assertEquals(MatchPhase.IDLE, MatchPhaseController.phase.value)
    }

    @Test
    fun idleWatchdogResetsPhase() {
        MatchPhaseController.reset()
        MatchPhaseController.onLoginTraffic()
        assertEquals(MatchPhase.QUEUE, MatchPhaseController.phase.value)

        val now = System.currentTimeMillis() + 120_000
        MatchPhaseController.resetIfIdle(now, idleAfterMs = 90_000)
        assertEquals(MatchPhase.IDLE, MatchPhaseController.phase.value)
    }

    @Test
    fun sessionEndResetsToIdle() {
        MatchPhaseController.reset()
        MatchPhaseController.onLoginTraffic()
        MatchPhaseController.onMatchFound()
        MatchPhaseController.onSessionEnd()
        assertEquals(MatchPhase.IDLE, MatchPhaseController.phase.value)
    }

    @Test
    fun startQueueEntersQueueExplicitly() {
        MatchPhaseController.reset()
        MatchPhaseController.startQueue()
        assertEquals(MatchPhase.QUEUE, MatchPhaseController.phase.value)

        // Повторный старт не ломает найденный матч.
        MatchPhaseController.onMatchFound()
        MatchPhaseController.startQueue()
        assertEquals(MatchPhase.MATCH_FOUND, MatchPhaseController.phase.value)
    }

    @Test
    fun filterMapping() {        assertEquals(PhaseFilter.ACTIVE, MatchPhaseController.filterFor(MatchPhase.IDLE))
        assertEquals(PhaseFilter.ACTIVE, MatchPhaseController.filterFor(MatchPhase.QUEUE))
        assertEquals(PhaseFilter.BLOCKS_OFF, MatchPhaseController.filterFor(MatchPhase.MATCH_FOUND))
        assertEquals(PhaseFilter.DELAY_ONLY, MatchPhaseController.filterFor(MatchPhase.IN_GAME))
    }
}
