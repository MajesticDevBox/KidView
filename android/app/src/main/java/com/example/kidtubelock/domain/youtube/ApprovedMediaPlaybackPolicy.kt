package com.example.kidtubelock.domain.youtube

import com.example.kidtubelock.domain.model.ApprovedMediaItem
import com.example.kidtubelock.domain.model.MediaType

enum class PlaybackAvailability {
    PLAYABLE_NOW,
    UNSUPPORTED_PLAYLIST,
    UNAVAILABLE,
}

data class PlaybackSupport(
    val availability: PlaybackAvailability,
    val isPlayableNow: Boolean,
    val statusLabel: String,
    val detailMessage: String,
)

object ApprovedMediaPlaybackPolicy {
    fun supportFor(item: ApprovedMediaItem?): PlaybackSupport {
        return when (item?.mediaType) {
            MediaType.VIDEO -> PlaybackSupport(
                availability = PlaybackAvailability.PLAYABLE_NOW,
                isPlayableNow = true,
                statusLabel = "Playable now",
                detailMessage = "This approved video can launch in child mode right now.",
            )

            MediaType.PLAYLIST -> when {
                item.playlistEntries.isNotEmpty() -> PlaybackSupport(
                    availability = PlaybackAvailability.PLAYABLE_NOW,
                    isPlayableNow = true,
                    statusLabel = "Playlist ready",
                    detailMessage = "This curated playlist can launch in child mode right now.",
                )

                item.youtubeId.isNotBlank() -> PlaybackSupport(
                    availability = PlaybackAvailability.PLAYABLE_NOW,
                    isPlayableNow = true,
                    statusLabel = "Playlist link ready",
                    detailMessage = "This YouTube playlist link can launch in child mode right now.",
                )

                else -> PlaybackSupport(
                    availability = PlaybackAvailability.UNSUPPORTED_PLAYLIST,
                    isPlayableNow = false,
                    statusLabel = "Unavailable",
                    detailMessage = "Add a YouTube playlist link or at least one playlist video before starting child mode.",
                )
            }

            null -> PlaybackSupport(
                availability = PlaybackAvailability.UNAVAILABLE,
                isPlayableNow = false,
                statusLabel = "Unavailable",
                detailMessage = "The selected approved item is no longer available.",
            )
        }
    }
}
