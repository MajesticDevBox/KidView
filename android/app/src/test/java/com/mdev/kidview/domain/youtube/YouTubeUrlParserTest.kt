package com.mdev.kidview.domain.youtube

import com.mdev.kidview.domain.model.MediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeUrlParserTest {
    @Test
    fun parse_detectsWatchVideoUrl() {
        val parsed = YouTubeUrlParser.parse("https://www.youtube.com/watch?v=abc123XYZ")

        assertEquals(MediaType.VIDEO, parsed?.mediaType)
        assertEquals("abc123XYZ", parsed?.youtubeId)
    }

    @Test
    fun parse_detectsShortShareUrl() {
        val parsed = YouTubeUrlParser.parse("https://youtu.be/abc123XYZ")

        assertEquals(MediaType.VIDEO, parsed?.mediaType)
        assertEquals("abc123XYZ", parsed?.youtubeId)
    }

    @Test
    fun parse_detectsPlaylistUrl() {
        val parsed = YouTubeUrlParser.parse(
            "https://www.youtube.com/playlist?list=PL1234567890",
        )

        assertEquals(MediaType.PLAYLIST, parsed?.mediaType)
        assertEquals("PL1234567890", parsed?.youtubeId)
    }

    @Test
    fun parse_prefersVideoWhenWatchUrlContainsVideoAndPlaylist() {
        val parsed = YouTubeUrlParser.parse(
            "https://www.youtube.com/watch?v=abc123XYZ&list=PL1234567890",
        )

        assertEquals(MediaType.VIDEO, parsed?.mediaType)
        assertEquals("abc123XYZ", parsed?.youtubeId)
    }

    @Test
    fun parse_rejectsUnsupportedUrl() {
        assertNull(YouTubeUrlParser.parse("https://example.com/watch?v=abc123XYZ"))
    }
}
