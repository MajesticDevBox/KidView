package com.mdev.kidview.domain.model

import com.mdev.kidview.feature_lock.LockMode

data class AppSettings(
    val hasParentPin: Boolean = false,
    val approvedMedia: List<ApprovedMediaItem> = emptyList(),
    val selectedApprovedMediaItemId: String? = null,
    val preferredLockMode: LockMode = LockMode.STANDARD_PHONE,
    val timeLimitMinutes: Int? = null,
)
