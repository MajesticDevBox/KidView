package com.mdev.kidview.domain.model

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeLimitFormatterTest {

    @Test
    fun summary_returnsOffWhenNoLimitIsSet() {
        assertEquals("Off", TimeLimitFormatter.summary(null))
    }

    @Test
    fun timerFace_formatsMinutesAsClockStyleLabel() {
        assertEquals("30:00", TimeLimitFormatter.timerFace(30))
    }

    @Test
    fun endAtLabel_usesCurrentTimePlusMinutes() {
        assertEquals(
            "Ends at 3:30 PM",
            TimeLimitFormatter.endAtLabel(30, now = LocalTime.of(15, 0)),
        )
    }
}
