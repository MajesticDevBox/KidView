package com.example.kidtubelock.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ApprovedMediaItem(
    val localId: String,
    val mediaType: MediaType,
    val youtubeId: String,
    val originalUrl: String,
    val displayTitle: String = "",
    val displaySubtitle: String = "",
    val contextNote: String = "",
    val expectedItemCount: Int? = null,
    val thumbnailUrl: String = "",
    val playlistEntries: List<PlaylistVideoEntry> = emptyList(),
    val addedAtEpochMillis: Long,
)

@Serializable
data class PlaylistVideoEntry(
    val localId: String,
    val youtubeId: String,
    val originalUrl: String,
    val displayTitle: String = "",
    val displaySubtitle: String = "",
    val thumbnailUrl: String = "",
    val addedAtEpochMillis: Long,
)
