package com.mdev.kidview.domain.model

import java.util.Locale

data class ChildModeTimeLimitStatus(
    val limitMinutes: Int? = null,
    val remainingSeconds: Long? = null,
    val isReached: Boolean = false,
)

object ChildModeTimeLimitPolicy {
    fun status(
        limitMinutes: Int?,
        elapsedSeconds: Long,
    ): ChildModeTimeLimitStatus {
        if (limitMinutes == null || limitMinutes <= 0) {
            return ChildModeTimeLimitStatus()
        }

        val totalSeconds = limitMinutes * 60L
        val remainingSeconds = (totalSeconds - elapsedSeconds).coerceAtLeast(0L)

        return ChildModeTimeLimitStatus(
            limitMinutes = limitMinutes,
            remainingSeconds = remainingSeconds,
            isReached = elapsedSeconds >= totalSeconds,
        )
    }

    fun remainingLabel(
        remainingSeconds: Long?,
    ): String? {
        if (remainingSeconds == null) {
            return null
        }

        val minutes = remainingSeconds / 60L
        val seconds = remainingSeconds % 60L
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
