package com.example.kidtubelock.data.youtube

import java.net.HttpURLConnection
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class ImportedYouTubeVideoMetadata(
    val title: String,
    val authorName: String,
    val thumbnailUrl: String,
)

@Singleton
class YouTubeVideoMetadataImporter @Inject constructor(
    private val json: Json,
) {
    suspend fun importFromUrl(url: String): ImportedYouTubeVideoMetadata? = withContext(Dispatchers.IO) {
        val encodedUrl = URLEncoder.encode(url, Charsets.UTF_8.name())
        val connection = java.net.URL(
            "https://www.youtube.com/oembed?url=$encodedUrl&format=json",
        ).openConnection() as HttpURLConnection

        runCatching {
            connection.requestMethod = "GET"
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000
            connection.setRequestProperty("Accept", "application/json")

            if (connection.responseCode !in 200..299) {
                return@withContext null
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val response = json.decodeFromString(OEmbedResponse.serializer(), responseText)
            ImportedYouTubeVideoMetadata(
                title = response.title.trim(),
                authorName = response.authorName.trim(),
                thumbnailUrl = response.thumbnailUrl.trim(),
            )
        }.getOrNull().also {
            connection.disconnect()
        }
    }

    @Serializable
    private data class OEmbedResponse(
        val title: String = "",
        @SerialName("author_name")
        val authorName: String = "",
        @SerialName("thumbnail_url")
        val thumbnailUrl: String = "",
    )
}
