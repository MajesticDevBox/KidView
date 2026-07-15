package com.example.kidtubelock.domain.repository

import com.example.kidtubelock.domain.model.AppSettings
import com.example.kidtubelock.domain.model.ApprovedMediaItem
import com.example.kidtubelock.feature_lock.LockMode
import kotlinx.coroutines.flow.Flow

interface ParentControlsRepository {
    val appSettings: Flow<AppSettings>

    suspend fun saveParentPin(pin: String)

    suspend fun verifyParentPin(pin: String): Boolean

    suspend fun addApprovedMedia(item: ApprovedMediaItem)

    suspend fun updateApprovedMedia(item: ApprovedMediaItem)

    suspend fun deleteApprovedMedia(itemId: String)

    suspend fun selectApprovedMedia(itemId: String?)

    suspend fun setPreferredLockMode(lockMode: LockMode)

    suspend fun setTimeLimitMinutes(minutes: Int?)

    fun approvedMediaItem(itemId: String): Flow<ApprovedMediaItem?>
}
