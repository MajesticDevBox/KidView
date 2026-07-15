package com.example.kidtubelock.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ApprovedMediaDisplayFormatterTest {

    @Test
    fun title_prefersStoredDisplayTitle() {
        val item = approvedMediaItem(
            displayTitle = "Animal Songs",
        )

        assertEquals("Animal Songs", ApprovedMediaDisplayFormatter.title(item, index = 4))
    }

    @Test
    fun title_fallsBackToPlaylistLabelWhenNoStoredTitle() {
        val item = approvedMediaItem(
            mediaType = MediaType.PLAYLIST,
            displayTitle = "",
        )

        assertEquals("Approved Playlist 2", ApprovedMediaDisplayFormatter.title(item, index = 1))
    }

    @Test
    fun subtitle_prefersStoredSubtitle() {
        val item = approvedMediaItem(
            displaySubtitle = "Bedtime mix",
            expectedItemCount = 12,
        )

        assertEquals("Bedtime mix", ApprovedMediaDisplayFormatter.subtitle(item, playbackStatus = "Playable now"))
    }

    @Test
    fun subtitle_usesExpectedItemCountWhenStoredSubtitleMissing() {
        val item = approvedMediaItem(
            mediaType = MediaType.PLAYLIST,
            displaySubtitle = "",
            expectedItemCount = 8,
        )

        assertEquals("8 videos", ApprovedMediaDisplayFormatter.subtitle(item))
    }

    @Test
    fun context_prefersStoredContextNote() {
        val item = approvedMediaItem(
            contextNote = "Starts with the dinosaur alphabet video.",
        )

        assertEquals(
            "Starts with the dinosaur alphabet video.",
            ApprovedMediaDisplayFormatter.context(item, fallback = "https://youtube.com/watch?v=test"),
        )
    }

    @Test
    fun context_fallsBackToProvidedValueWhenNoteMissing() {
        val item = approvedMediaItem(
            contextNote = "",
        )

        assertEquals(
            "https://youtube.com/watch?v=test",
            ApprovedMediaDisplayFormatter.context(item, fallback = "https://youtube.com/watch?v=test"),
        )
    }

    private fun approvedMediaItem(
        mediaType: MediaType = MediaType.VIDEO,
        displayTitle: String = "",
        displaySubtitle: String = "",
        contextNote: String = "",
        expectedItemCount: Int? = 1,
    ): ApprovedMediaItem = ApprovedMediaItem(
        localId = "local-id",
        mediaType = mediaType,
        youtubeId = "video-id",
        originalUrl = "https://youtube.com/watch?v=video-id",
        displayTitle = displayTitle,
        displaySubtitle = displaySubtitle,
        contextNote = contextNote,
        expectedItemCount = expectedItemCount,
        addedAtEpochMillis = 0L,
    )
}
