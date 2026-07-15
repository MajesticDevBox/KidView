package com.example.kidtubelock.feature_lock

object LockModePreferenceMapper {
    fun decode(rawValue: String?): LockMode {
        return runCatching {
            rawValue?.let(LockMode::valueOf)
        }.getOrNull() ?: LockMode.STANDARD_PHONE
    }

    fun encode(lockMode: LockMode): String = lockMode.name
}

