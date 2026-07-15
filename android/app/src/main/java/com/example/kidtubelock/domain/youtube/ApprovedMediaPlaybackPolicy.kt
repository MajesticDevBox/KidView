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

            MediaType.PLAYLIST -> if (item.playlistEntries.isNotEmpty()) {
                PlaybackSupport(
                    availability = PlaybackAvailability.PLAYABLE_NOW,
                    isPlayableNow = true,
                    statusLabel = "Playlist ready",
                    detailMessage = "This curated playlist can launch in child mode right now.",
                )
            } else {
                PlaybackSupport(
                    availability = PlaybackAvailability.UNSUPPORTED_PLAYLIST,
                    isPlayableNow = false,
                    statusLabel = "Stored only",
                    detailMessage = "Legacy YouTube playlist links are still stored, but custom KidTubeLock playlists are the supported playback path.",
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
