package com.example.kidtubelock.feature_player.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeEmbedHtmlBuilderTest {
    @Test
    fun buildVideoHtml_usesPrivacyEnhancedEmbedUrl() {
        val html = YouTubeEmbedHtmlBuilder.buildVideoHtml("abc123")

        assertTrue(html.contains("https://www.youtube-nocookie.com"))
        assertTrue(html.contains("videoId: 'abc123'"))
        assertTrue(html.contains("autoplay: 1"))
    }

    @Test
    fun buildVideoHtml_sanitizesUnexpectedCharacters() {
        val html = YouTubeEmbedHtmlBuilder.buildVideoHtml("abc123<script>")

        assertFalse(html.contains("abc123<script>"))
        assertTrue(html.contains("videoId: 'abc123script'"))
    }

    @Test
    fun buildPlaylistHtml_includesPlaylistIdsForCustomPlaylistPlayback() {
        val html = YouTubeEmbedHtmlBuilder.buildPlaylistHtml(
            listOf("abc123", "def456"),
        )

        assertTrue(html.contains("playlistIds = [\"abc123\",\"def456\"]"))
        assertTrue(html.contains("loadPlaylist"))
    }
}
