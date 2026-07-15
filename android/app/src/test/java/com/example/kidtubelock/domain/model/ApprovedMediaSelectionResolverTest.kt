package com.example.kidtubelock.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApprovedMediaSelectionResolverTest {
    private val firstItem = ApprovedMediaItem(
        localId = "first",
        mediaType = MediaType.VIDEO,
        youtubeId = "video1",
        originalUrl = "https://youtu.be/video1",
        addedAtEpochMillis = 1L,
    )
    private val secondItem = ApprovedMediaItem(
        localId = "second",
        mediaType = MediaType.PLAYLIST,
        youtubeId = "playlist1",
        originalUrl = "https://www.youtube.com/playlist?list=playlist1",
        addedAtEpochMillis = 2L,
    )

    @Test
    fun resolveSelectedItemId_returnsNullWhenListIsEmpty() {
        val resolved = ApprovedMediaSelectionResolver.resolveSelectedItemId(
            approvedMedia = emptyList(),
            requestedSelectedItemId = "missing",
        )

        assertNull(resolved)
    }

    @Test
    fun resolveSelectedItemId_keepsValidSelection() {
        val resolved = ApprovedMediaSelectionResolver.resolveSelectedItemId(
            approvedMedia = listOf(firstItem, secondItem),
            requestedSelectedItemId = "second",
        )

        assertEquals("second", resolved)
    }

    @Test
    fun resolveSelectedItemId_fallsBackToFirstAvailableItem() {
        val resolved = ApprovedMediaSelectionResolver.resolveSelectedItemId(
            approvedMedia = listOf(firstItem, secondItem),
            requestedSelectedItemId = "missing",
        )

        assertEquals("first", resolved)
    }
}
