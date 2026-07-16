package com.mdev.kidview.domain.model

object TimeLimitProgress {
    fun normalized(minutes: Int?, maxMinutes: Int = 60): Float {
        if (minutes == null || maxMinutes <= 0) {
            return 0f
        }

        return (minutes.toFloat() / maxMinutes.toFloat()).coerceIn(0f, 1f)
    }
}
