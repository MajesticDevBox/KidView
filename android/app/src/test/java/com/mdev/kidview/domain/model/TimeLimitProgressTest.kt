package com.mdev.kidview.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeLimitProgressTest {

    @Test
    fun normalized_returnsZeroWhenLimitIsOff() {
        assertEquals(0f, TimeLimitProgress.normalized(null), 0.0001f)
    }

    @Test
    fun normalized_returnsHalfForThirtyMinuteDefaultLimit() {
        assertEquals(0.5f, TimeLimitProgress.normalized(30), 0.0001f)
    }

    @Test
    fun normalized_clampsToFullProgress() {
        assertEquals(1f, TimeLimitProgress.normalized(90), 0.0001f)
    }
}
