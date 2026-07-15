package com.example.kidtubelock.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChildModeTimeLimitPolicyTest {

    @Test
    fun status_returnsNoLimitWhenTimerIsOff() {
        val status = ChildModeTimeLimitPolicy.status(
            limitMinutes = null,
            elapsedSeconds = 25L,
        )

        assertEquals(null, status.limitMinutes)
        assertEquals(null, status.remainingSeconds)
        assertFalse(status.isReached)
    }

    @Test
    fun status_returnsRemainingSecondsBeforeTimeLimitIsReached() {
        val status = ChildModeTimeLimitPolicy.status(
            limitMinutes = 15,
            elapsedSeconds = 120L,
        )

        assertEquals(15, status.limitMinutes)
        assertEquals(780L, status.remainingSeconds)
        assertFalse(status.isReached)
    }

    @Test
    fun status_marksReachedWhenElapsedTimePassesLimit() {
        val status = ChildModeTimeLimitPolicy.status(
            limitMinutes = 15,
            elapsedSeconds = 901L,
        )

        assertEquals(0L, status.remainingSeconds)
        assertTrue(status.isReached)
    }

    @Test
    fun remainingLabel_formatsAsMinutesAndSeconds() {
        assertEquals("13:00", ChildModeTimeLimitPolicy.remainingLabel(780L))
    }
}
