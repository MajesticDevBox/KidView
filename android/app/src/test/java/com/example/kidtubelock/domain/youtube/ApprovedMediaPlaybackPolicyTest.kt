package com.example.kidtubelock.domain.youtube

import com.example.kidtubelock.domain.model.ApprovedMediaItem
import com.example.kidtubelock.domain.model.MediaType
import com.example.kidtubelock.domain.model.PlaylistVideoEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApprovedMediaPlaybackPolicyTest {
    @Test
    fun supportFor_videoIsPlayableNow() {
        val item = ApprovedMediaItem(
            localId = "video",
            mediaType = MediaType.VIDEO,
            youtubeId = "abc123",
            originalUrl = "https://youtu.be/abc123",
            addedAtEpochMillis = 1L,
        )

        val support = ApprovedMediaPlaybackPolicy.supportFor(item)

        assertEquals(PlaybackAvailability.PLAYABLE_NOW, support.availability)
        assertTrue(support.isPlayableNow)
    }

    @Test
    fun supportFor_customPlaylistIsPlayableNow() {
        val item = ApprovedMediaItem(
            localId = "playlist",
            mediaType = MediaType.PLAYLIST,
            youtubeId = "abc123",
            originalUrl = "",
            playlistEntries = listOf(
                PlaylistVideoEntry(
                    localId = "entry-1",
                    youtubeId = "abc123",
                    originalUrl = "https://youtu.be/abc123",
                    displayTitle = "First video",
                    addedAtEpochMillis = 1L,
                ),
            ),
            addedAtEpochMillis = 1L,
        )

        val support = ApprovedMediaPlaybackPolicy.supportFor(item)

        assertEquals(PlaybackAvailability.PLAYABLE_NOW, support.availability)
        assertTrue(support.isPlayableNow)
        assertEquals("Playlist ready", support.statusLabel)
    }

    @Test
    fun supportFor_directPlaylistLinkIsPlayableNow() {
        val item = ApprovedMediaItem(
            localId = "playlist",
            mediaType = MediaType.PLAYLIST,
            youtubeId = "PL123",
            originalUrl = "https://www.youtube.com/playlist?list=PL123",
            addedAtEpochMillis = 1L,
        )

        val support = ApprovedMediaPlaybackPolicy.supportFor(item)

        assertEquals(PlaybackAvailability.PLAYABLE_NOW, support.availability)
        assertTrue(support.isPlayableNow)
        assertEquals("Playlist link ready", support.statusLabel)
    }
}
