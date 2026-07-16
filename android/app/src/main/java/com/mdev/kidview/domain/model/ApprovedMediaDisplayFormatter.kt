package com.mdev.kidview.domain.model

object ApprovedMediaDisplayFormatter {
    fun title(item: ApprovedMediaItem, index: Int? = null): String = item.displayTitle.ifBlank {
        when (item.mediaType) {
            MediaType.VIDEO -> index?.let { "Approved Video ${it + 1}" } ?: "Approved Video"
            MediaType.PLAYLIST -> index?.let { "Approved Playlist ${it + 1}" } ?: "Approved Playlist"
        }
    }

    fun subtitle(item: ApprovedMediaItem, playbackStatus: String? = null): String {
        if (item.displaySubtitle.isNotBlank()) {
            return item.displaySubtitle
        }

        if (item.mediaType == MediaType.PLAYLIST && item.playlistEntries.isNotEmpty()) {
            val count = item.playlistEntries.size
            return if (count == 1) {
                "1 video"
            } else {
                "$count videos"
            }
        }

        item.expectedItemCount?.takeIf { it > 0 }?.let { count ->
            return if (count == 1) {
                "1 video"
            } else {
                "$count videos"
            }
        }

        if (!playbackStatus.isNullOrBlank()) {
            return playbackStatus
        }

        return when (item.mediaType) {
            MediaType.VIDEO -> "Saved video link"
            MediaType.PLAYLIST -> "Saved playlist link"
        }
    }

    fun context(item: ApprovedMediaItem, fallback: String? = null): String {
        if (item.contextNote.isNotBlank()) {
            return item.contextNote
        }

        if (item.mediaType == MediaType.PLAYLIST && item.playlistEntries.isNotEmpty()) {
            val firstVideoTitle = item.playlistEntries.first().displayTitle.ifBlank { null }
            return firstVideoTitle?.let { "Starts with $it" }.orEmpty()
        }

        return fallback.orEmpty()
    }
}
