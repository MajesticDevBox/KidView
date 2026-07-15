package com.example.kidtubelock.feature_parent.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtubelock.app.AppDestination
import com.example.kidtubelock.data.youtube.ImportedYouTubeVideoMetadata
import com.example.kidtubelock.data.youtube.YouTubeVideoMetadataImporter
import com.example.kidtubelock.domain.model.ApprovedMediaItem
import com.example.kidtubelock.domain.model.ApprovedMediaDisplayFormatter
import com.example.kidtubelock.domain.model.MediaType
import com.example.kidtubelock.domain.model.PlaylistVideoEntry
import com.example.kidtubelock.domain.repository.ParentControlsRepository
import com.example.kidtubelock.domain.youtube.YouTubeUrlParser
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PlaylistOption(
    val localId: String,
    val title: String,
    val videoCount: Int,
)

enum class VideoSaveTarget {
    STANDALONE,
    EXISTING_PLAYLIST,
}

data class AddMediaUiState(
    val mode: AddMediaMode = AddMediaMode.VIDEO,
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val url: String = "",
    val title: String = "",
    val subtitle: String = "",
    val contextNote: String = "",
    val previewMediaType: MediaType? = null,
    val isImportingMetadata: Boolean = false,
    val importMessage: String? = null,
    val importedThumbnailUrl: String = "",
    val videoSaveTarget: VideoSaveTarget = VideoSaveTarget.STANDALONE,
    val selectedPlaylistId: String? = null,
    val playlistUrl: String = "",
    val availablePlaylists: List<PlaylistOption> = emptyList(),
    val playlistVideoUrl: String = "",
    val isImportingPlaylistVideo: Boolean = false,
    val playlistImportMessage: String? = null,
    val pendingPlaylistVideo: PlaylistVideoEntry? = null,
    val playlistEntries: List<PlaylistVideoEntry> = emptyList(),
)

@HiltViewModel
class AddMediaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ParentControlsRepository,
    private val metadataImporter: YouTubeVideoMetadataImporter,
) : ViewModel() {
    private val editingItemId: String? = savedStateHandle[AppDestination.EditMedia.argumentName]
    private val requestedMode = AddMediaMode.fromRouteValue(
        savedStateHandle[AppDestination.AddMedia.argumentName],
    )

    private var existingItem: ApprovedMediaItem? = null
    private var hasLoadedInitialState = false
    private var videoImportJob: Job? = null
    private var playlistVideoImportJob: Job? = null
    private var titleWasAutoFilled = false
    private var subtitleWasAutoFilled = false

    var uiState by mutableStateOf(
        AddMediaUiState(mode = requestedMode),
    )
        private set

    init {
        viewModelScope.launch {
            repository.appSettings.collect { settings ->
                val availablePlaylists = settings.approvedMedia
                    .filter { it.mediaType == MediaType.PLAYLIST && it.localId != editingItemId }
                    .map { item ->
                        PlaylistOption(
                            localId = item.localId,
                            title = ApprovedMediaDisplayFormatter.title(item),
                            videoCount = item.playlistEntries.size.takeIf { count -> count > 0 }
                                ?: item.expectedItemCount
                                ?: 0,
                        )
                    }

                if (editingItemId != null && !hasLoadedInitialState) {
                    val item = settings.approvedMedia.firstOrNull { it.localId == editingItemId }
                    if (item != null) {
                        existingItem = item
                        uiState = when (item.mediaType) {
                            MediaType.VIDEO -> AddMediaUiState(
                                mode = AddMediaMode.VIDEO,
                                isEditMode = true,
                                url = item.originalUrl,
                                title = item.displayTitle,
                                subtitle = item.displaySubtitle,
                                contextNote = item.contextNote,
                                previewMediaType = MediaType.VIDEO,
                                importedThumbnailUrl = item.thumbnailUrl,
                                availablePlaylists = availablePlaylists,
                            )

                            MediaType.PLAYLIST -> AddMediaUiState(
                                mode = AddMediaMode.PLAYLIST,
                                isEditMode = true,
                                playlistUrl = item.originalUrl,
                                title = item.displayTitle,
                                subtitle = item.displaySubtitle,
                                contextNote = item.contextNote,
                                availablePlaylists = availablePlaylists,
                                playlistEntries = item.playlistEntries,
                            )
                        }
                        hasLoadedInitialState = true
                    }
                } else {
                    uiState = uiState.copy(
                        availablePlaylists = availablePlaylists,
                        selectedPlaylistId = when {
                            uiState.videoSaveTarget != VideoSaveTarget.EXISTING_PLAYLIST -> uiState.selectedPlaylistId
                            availablePlaylists.any { it.localId == uiState.selectedPlaylistId } -> uiState.selectedPlaylistId
                            else -> availablePlaylists.firstOrNull()?.localId
                        },
                    )
                }
            }
        }
    }

    fun onUrlChanged(value: String) {
        uiState = uiState.copy(
            url = value,
            errorMessage = null,
            previewMediaType = YouTubeUrlParser.parse(value)?.mediaType,
        )
        importVideoMetadata(value)
    }

    fun onTitleChanged(value: String) {
        titleWasAutoFilled = false
        uiState = uiState.copy(title = value, errorMessage = null)
    }

    fun onSubtitleChanged(value: String) {
        subtitleWasAutoFilled = false
        uiState = uiState.copy(subtitle = value, errorMessage = null)
    }

    fun onContextNoteChanged(value: String) {
        uiState = uiState.copy(contextNote = value, errorMessage = null)
    }

    fun onVideoSaveTargetSelected(target: VideoSaveTarget) {
        uiState = uiState.copy(
            videoSaveTarget = target,
            selectedPlaylistId = when (target) {
                VideoSaveTarget.STANDALONE -> null
                VideoSaveTarget.EXISTING_PLAYLIST -> uiState.selectedPlaylistId
                    ?: uiState.availablePlaylists.firstOrNull()?.localId
            },
            errorMessage = null,
        )
    }

    fun onSelectedPlaylistChanged(playlistId: String) {
        uiState = uiState.copy(
            selectedPlaylistId = playlistId,
            errorMessage = null,
        )
    }

    fun onPlaylistVideoUrlChanged(value: String) {
        uiState = uiState.copy(
            playlistVideoUrl = value,
            pendingPlaylistVideo = null,
            playlistImportMessage = null,
            errorMessage = null,
        )
        importPlaylistVideoMetadata(value)
    }

    fun onPlaylistUrlChanged(value: String) {
        uiState = uiState.copy(
            playlistUrl = value,
            errorMessage = null,
        )
    }

    fun addPendingVideoToPlaylist() {
        val pendingVideo = uiState.pendingPlaylistVideo
        if (pendingVideo == null) {
            uiState = uiState.copy(
                errorMessage = "Paste a valid YouTube video link and wait for the details to import.",
            )
            return
        }
        if (uiState.playlistEntries.any { it.youtubeId == pendingVideo.youtubeId }) {
            uiState = uiState.copy(
                errorMessage = "That video is already in this playlist.",
            )
            return
        }

        uiState = uiState.copy(
            playlistEntries = uiState.playlistEntries + pendingVideo,
            playlistVideoUrl = "",
            pendingPlaylistVideo = null,
            playlistImportMessage = "${pendingVideo.displayTitle} added to this playlist.",
            errorMessage = null,
        )
    }

    fun removePlaylistEntry(entryId: String) {
        uiState = uiState.copy(
            playlistEntries = uiState.playlistEntries.filterNot { it.localId == entryId },
            errorMessage = null,
        )
    }

    fun saveApprovedMedia(onSuccess: () -> Unit) {
        when (uiState.mode) {
            AddMediaMode.VIDEO -> saveVideo(onSuccess)
            AddMediaMode.PLAYLIST -> savePlaylist(onSuccess)
        }
    }

    private fun saveVideo(onSuccess: () -> Unit) {
        val parsed = YouTubeUrlParser.parse(uiState.url)
        if (parsed?.mediaType != MediaType.VIDEO) {
            uiState = uiState.copy(
                errorMessage = "Add Video only supports single YouTube video links.",
            )
            return
        }
        if (uiState.title.trim().isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Wait for the title to import or enter one manually.",
            )
            return
        }
        if (!uiState.isEditMode &&
            uiState.videoSaveTarget == VideoSaveTarget.EXISTING_PLAYLIST &&
            uiState.selectedPlaylistId == null
        ) {
            uiState = uiState.copy(
                errorMessage = "Choose a playlist to attach this video to.",
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true, errorMessage = null)
            val now = System.currentTimeMillis()
            val playlistEntry = PlaylistVideoEntry(
                localId = UUID.randomUUID().toString(),
                youtubeId = parsed.youtubeId,
                originalUrl = uiState.url.trim(),
                displayTitle = uiState.title.trim(),
                displaySubtitle = uiState.subtitle.trim(),
                thumbnailUrl = uiState.importedThumbnailUrl,
                addedAtEpochMillis = now,
            )

            if (uiState.isEditMode) {
                val item = existingItem
                if (item != null) {
                    repository.updateApprovedMedia(
                        item.copy(
                            mediaType = MediaType.VIDEO,
                            youtubeId = parsed.youtubeId,
                            originalUrl = uiState.url.trim(),
                            displayTitle = uiState.title.trim(),
                            displaySubtitle = uiState.subtitle.trim(),
                            contextNote = uiState.contextNote.trim(),
                            expectedItemCount = 1,
                            thumbnailUrl = uiState.importedThumbnailUrl,
                        ),
                    )
                }
            } else if (uiState.videoSaveTarget == VideoSaveTarget.EXISTING_PLAYLIST) {
                val settings = repository.appSettings.first()
                val playlist = settings.approvedMedia.firstOrNull { it.localId == uiState.selectedPlaylistId }
                if (playlist == null) {
                    uiState = uiState.copy(
                        isSaving = false,
                        errorMessage = "That playlist is no longer available.",
                    )
                    return@launch
                }
                val updatedEntries = playlist.playlistEntries + playlistEntry
                repository.updateApprovedMedia(
                    playlist.copy(
                        youtubeId = updatedEntries.firstOrNull()?.youtubeId ?: playlist.youtubeId,
                        expectedItemCount = updatedEntries.size,
                        thumbnailUrl = playlist.thumbnailUrl.ifBlank { playlistEntry.thumbnailUrl },
                        playlistEntries = updatedEntries,
                    ),
                )
            } else {
                repository.addApprovedMedia(
                    ApprovedMediaItem(
                        localId = UUID.randomUUID().toString(),
                        mediaType = MediaType.VIDEO,
                        youtubeId = parsed.youtubeId,
                        originalUrl = uiState.url.trim(),
                        displayTitle = uiState.title.trim(),
                        displaySubtitle = uiState.subtitle.trim(),
                        contextNote = uiState.contextNote.trim(),
                        expectedItemCount = 1,
                        thumbnailUrl = uiState.importedThumbnailUrl,
                        addedAtEpochMillis = now,
                    ),
                )
            }
            uiState = uiState.copy(isSaving = false)
            onSuccess()
        }
    }

    private fun savePlaylist(onSuccess: () -> Unit) {
        val parsedPlaylist = YouTubeUrlParser.parse(uiState.playlistUrl)
            ?.takeIf { it.mediaType == MediaType.PLAYLIST }
        if (uiState.title.trim().isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Enter a playlist name so it is easy to recognize.",
            )
            return
        }
        if (uiState.playlistEntries.isEmpty() && parsedPlaylist == null) {
            uiState = uiState.copy(
                errorMessage = "Paste a YouTube playlist link or add at least one video before saving.",
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true, errorMessage = null)
            val item = existingItem
            val now = System.currentTimeMillis()
            val playlistEntries = uiState.playlistEntries
            val youtubeId = playlistEntries.firstOrNull()?.youtubeId
                ?: parsedPlaylist!!.youtubeId
            val playlistOriginalUrl = uiState.playlistUrl.trim().ifBlank { item?.originalUrl.orEmpty() }
            val thumbnailUrl = playlistEntries.firstOrNull()?.thumbnailUrl.orEmpty()
            val expectedItemCount = playlistEntries.size.takeIf { it > 0 }

            if (uiState.isEditMode && item != null) {
                repository.updateApprovedMedia(
                    item.copy(
                        mediaType = MediaType.PLAYLIST,
                        youtubeId = youtubeId,
                        originalUrl = playlistOriginalUrl,
                        displayTitle = uiState.title.trim(),
                        displaySubtitle = uiState.subtitle.trim(),
                        contextNote = uiState.contextNote.trim(),
                        expectedItemCount = expectedItemCount,
                        thumbnailUrl = thumbnailUrl,
                        playlistEntries = playlistEntries,
                    ),
                )
            } else {
                repository.addApprovedMedia(
                    ApprovedMediaItem(
                        localId = UUID.randomUUID().toString(),
                        mediaType = MediaType.PLAYLIST,
                        youtubeId = youtubeId,
                        originalUrl = playlistOriginalUrl,
                        displayTitle = uiState.title.trim(),
                        displaySubtitle = uiState.subtitle.trim(),
                        contextNote = uiState.contextNote.trim(),
                        expectedItemCount = expectedItemCount,
                        thumbnailUrl = thumbnailUrl,
                        playlistEntries = playlistEntries,
                        addedAtEpochMillis = now,
                    ),
                )
            }

            uiState = uiState.copy(isSaving = false)
            onSuccess()
        }
    }

    private fun importVideoMetadata(url: String) {
        videoImportJob?.cancel()
        if (uiState.mode != AddMediaMode.VIDEO) {
            return
        }

        val parsed = YouTubeUrlParser.parse(url)
        if (parsed?.mediaType != MediaType.VIDEO) {
            uiState = uiState.copy(
                isImportingMetadata = false,
                importMessage = if (url.isBlank()) null else "Paste a single YouTube video link to import details.",
                importedThumbnailUrl = if (url.isBlank()) uiState.importedThumbnailUrl else "",
            )
            return
        }

        videoImportJob = viewModelScope.launch {
            delay(250)
            uiState = uiState.copy(
                isImportingMetadata = true,
                importMessage = "Importing video details from YouTube...",
                errorMessage = null,
            )
            val metadata = metadataImporter.importFromUrl(url.trim())
            if (metadata == null) {
                uiState = uiState.copy(
                    isImportingMetadata = false,
                    importMessage = "Could not import details automatically. You can still save the video manually.",
                )
                return@launch
            }

            var updatedTitle = uiState.title
            var updatedSubtitle = uiState.subtitle
            if (updatedTitle.isBlank() || titleWasAutoFilled) {
                updatedTitle = metadata.title
                titleWasAutoFilled = true
            }
            if (updatedSubtitle.isBlank() || subtitleWasAutoFilled) {
                updatedSubtitle = metadata.authorName
                subtitleWasAutoFilled = true
            }

            uiState = uiState.copy(
                title = updatedTitle,
                subtitle = updatedSubtitle,
                importedThumbnailUrl = metadata.thumbnailUrl,
                isImportingMetadata = false,
                importMessage = "Imported title from YouTube.",
            )
        }
    }

    private fun importPlaylistVideoMetadata(url: String) {
        playlistVideoImportJob?.cancel()
        if (uiState.mode != AddMediaMode.PLAYLIST) {
            return
        }

        val parsed = YouTubeUrlParser.parse(url)
        if (parsed?.mediaType != MediaType.VIDEO) {
            uiState = uiState.copy(
                isImportingPlaylistVideo = false,
                playlistImportMessage = if (url.isBlank()) null else "Paste a single YouTube video link to add it to this playlist.",
                pendingPlaylistVideo = null,
            )
            return
        }

        playlistVideoImportJob = viewModelScope.launch {
            delay(250)
            uiState = uiState.copy(
                isImportingPlaylistVideo = true,
                playlistImportMessage = "Importing video details from YouTube...",
                errorMessage = null,
            )
            val metadata = metadataImporter.importFromUrl(url.trim())
            val pendingEntry = createPlaylistEntry(
                youtubeId = parsed.youtubeId,
                originalUrl = url.trim(),
                metadata = metadata,
            )
            uiState = uiState.copy(
                isImportingPlaylistVideo = false,
                pendingPlaylistVideo = pendingEntry,
                playlistImportMessage = if (metadata != null) {
                    "Imported ${pendingEntry.displayTitle}."
                } else {
                    "Could not import details automatically, but you can still add the video."
                },
            )
        }
    }

    private fun createPlaylistEntry(
        youtubeId: String,
        originalUrl: String,
        metadata: ImportedYouTubeVideoMetadata?,
    ): PlaylistVideoEntry = PlaylistVideoEntry(
        localId = UUID.randomUUID().toString(),
        youtubeId = youtubeId,
        originalUrl = originalUrl,
        displayTitle = metadata?.title.orEmpty().ifBlank { "Video ${uiState.playlistEntries.size + 1}" },
        displaySubtitle = metadata?.authorName.orEmpty(),
        thumbnailUrl = metadata?.thumbnailUrl.orEmpty(),
        addedAtEpochMillis = System.currentTimeMillis(),
    )
}
