package com.example.kidtubelock.domain.model

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeLimitFormatter {
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    fun summary(minutes: Int?): String = when (minutes) {
        null -> "Off"
        else -> "$minutes minutes"
    }

    fun timerFace(minutes: Int?): String = when (minutes) {
        null -> "--:--"
        else -> String.format(Locale.US, "%02d:00", minutes)
    }

    fun endAtLabel(minutes: Int?, now: LocalTime = LocalTime.now()): String = when (minutes) {
        null -> "No end time set"
        else -> "Ends at ${now.plusMinutes(minutes.toLong()).format(timeFormatter)}"
    }
}
