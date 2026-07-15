package com.example.kidtubelock.domain.model

import com.example.kidtubelock.feature_lock.LockMode

data class AppSettings(
    val hasParentPin: Boolean = false,
    val approvedMedia: List<ApprovedMediaItem> = emptyList(),
    val selectedApprovedMediaItemId: String? = null,
    val preferredLockMode: LockMode = LockMode.STANDARD_PHONE,
    val timeLimitMinutes: Int? = null,
)
