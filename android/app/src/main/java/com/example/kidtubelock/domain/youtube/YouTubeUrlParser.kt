package com.example.kidtubelock.domain.youtube

import com.example.kidtubelock.domain.model.MediaType
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class ParsedYouTubeMedia(
    val mediaType: MediaType,
    val youtubeId: String,
)

object YouTubeUrlParser {
    private val supportedHosts = setOf(
        "youtube.com",
        "www.youtube.com",
        "m.youtube.com",
        "youtu.be",
    )

    fun parse(rawUrl: String): ParsedYouTubeMedia? {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) {
            return null
        }

        val uri = runCatching { URI(trimmed) }.getOrNull() ?: return null
        val host = uri.host?.lowercase() ?: return null
        if (host !in supportedHosts) {
            return null
        }

        val queryParameters = uri.rawQuery
            .orEmpty()
            .split("&")
            .mapNotNull { queryPart ->
                if (queryPart.isBlank()) {
                    null
                } else {
                    val pieces = queryPart.split("=", limit = 2)
                    val key = decode(pieces[0])
                    val value = decode(pieces.getOrElse(1) { "" })
                    key to value
                }
            }
            .toMap()

        val pathSegments = uri.path
            ?.split("/")
            ?.filter { it.isNotBlank() }
            .orEmpty()

        return when {
            host == "youtu.be" -> {
                val id = pathSegments.firstOrNull()?.takeIf { it.isNotBlank() } ?: return null
                ParsedYouTubeMedia(MediaType.VIDEO, id)
            }

            uri.path == "/watch" -> {
                val videoId = queryParameters["v"]
                val playlistId = queryParameters["list"]

                when {
                    !videoId.isNullOrBlank() -> ParsedYouTubeMedia(MediaType.VIDEO, videoId)
                    !playlistId.isNullOrBlank() -> ParsedYouTubeMedia(MediaType.PLAYLIST, playlistId)
                    else -> null
                }
            }

            uri.path == "/playlist" -> {
                val playlistId = queryParameters["list"] ?: return null
                ParsedYouTubeMedia(MediaType.PLAYLIST, playlistId)
            }

            pathSegments.firstOrNull() in setOf("shorts", "embed", "live") -> {
                val id = pathSegments.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return null
                ParsedYouTubeMedia(MediaType.VIDEO, id)
            }

            else -> null
        }
    }

    private fun decode(value: String): String = URLDecoder.decode(
        value,
        StandardCharsets.UTF_8,
    )
}
