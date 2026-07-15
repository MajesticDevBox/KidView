package com.example.kidtubelock.feature_parent.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kidtubelock.domain.model.ApprovedMediaDisplayFormatter
import com.example.kidtubelock.domain.model.PlaylistVideoEntry
import com.example.kidtubelock.feature_parent.common.ParentScreenBackground
import com.example.kidtubelock.feature_parent.common.ParentSectionCard
import com.example.kidtubelock.feature_parent.common.parentPrimaryButtonColors
import com.example.kidtubelock.feature_parent.common.parentSecondaryButtonColors
import com.example.kidtubelock.feature_parent.common.parentTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMediaScreen(
    viewModel: AddMediaViewModel,
    onBack: () -> Unit,
    onMediaSaved: () -> Unit,
) {
    val uiState = viewModel.uiState
    val screenTitle = when {
        uiState.isEditMode && uiState.mode == AddMediaMode.PLAYLIST -> "Edit Playlist"
        uiState.isEditMode -> "Edit Video"
        uiState.mode == AddMediaMode.PLAYLIST -> "Add Playlist"
        else -> "Add Video"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                ),
                title = { Text(screenTitle) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        ParentScreenBackground(
            modifier = Modifier.padding(paddingValues),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    ParentSectionCard(
                        title = when (uiState.mode) {
                            AddMediaMode.VIDEO -> if (uiState.isEditMode) "Edit safe video" else "Add safe video"
                            AddMediaMode.PLAYLIST -> if (uiState.isEditMode) "Edit playlist" else "Add playlist"
                            else -> "Add media"
                        },
                        subtitle = when (uiState.mode) {
                            AddMediaMode.VIDEO -> "Paste one YouTube video link and the app will try to import its title."
                            AddMediaMode.PLAYLIST -> "Paste a YouTube playlist link or build a curated local playlist from approved videos."
                            else -> "Save approved media for child mode."
                        },
                    ) {
                        Text(
                            text = if (uiState.mode == AddMediaMode.VIDEO) {
                                "Add Video can save a single video on its own or attach it to an existing playlist."
                            } else {
                                "Add Playlist can save a direct YouTube playlist link or create a local curated playlist that you control video by video."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                when (uiState.mode) {
                    AddMediaMode.VIDEO -> {
                        item {
                            VideoFormSection(
                                uiState = uiState,
                                viewModel = viewModel,
                            )
                        }
                    }

                    AddMediaMode.PLAYLIST -> {
                        item {
                            PlaylistFormSection(
                                uiState = uiState,
                                viewModel = viewModel,
                            )
                        }
                    }
                }

                if (uiState.errorMessage != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            ),
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.saveApprovedMedia(onMediaSaved) },
                        enabled = !uiState.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = parentPrimaryButtonColors(),
                    ) {
                        Text(
                            when {
                                uiState.isSaving -> "Saving..."
                                uiState.isEditMode && uiState.mode == AddMediaMode.PLAYLIST -> "Save Playlist"
                                uiState.isEditMode -> "Save Video"
                                uiState.mode == AddMediaMode.PLAYLIST -> "Save Playlist"
                                uiState.videoSaveTarget == VideoSaveTarget.EXISTING_PLAYLIST -> "Add Video To Playlist"
                                else -> "Save Video"
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoFormSection(
    uiState: AddMediaUiState,
    viewModel: AddMediaViewModel,
) {
    ParentSectionCard(
        title = "Video link",
        subtitle = "Paste a single YouTube video URL.",
    ) {
        OutlinedTextField(
            value = uiState.url,
            onValueChange = viewModel::onUrlChanged,
            label = { Text("YouTube video URL") },
            modifier = Modifier.fillMaxWidth(),
            colors = parentTextFieldColors(),
            supportingText = {
                Text("Watch links and short links are supported.")
            },
        )

        ImportStatusCard(
            title = "YouTube import",
            message = when {
                uiState.isImportingMetadata -> "Importing title from YouTube..."
                !uiState.importMessage.isNullOrBlank() -> uiState.importMessage
                else -> "Paste a video link to auto-fill the title and channel name."
            },
        )

        OutlinedTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChanged,
            label = { Text("Video title") },
            modifier = Modifier.fillMaxWidth(),
            colors = parentTextFieldColors(),
        )

        OutlinedTextField(
            value = uiState.subtitle,
            onValueChange = viewModel::onSubtitleChanged,
            label = { Text("Channel or subtitle") },
            modifier = Modifier.fillMaxWidth(),
            colors = parentTextFieldColors(),
        )

        OutlinedTextField(
            value = uiState.contextNote,
            onValueChange = viewModel::onContextNoteChanged,
            label = { Text("Parent note") },
            modifier = Modifier.fillMaxWidth(),
            colors = parentTextFieldColors(),
            minLines = 3,
            supportingText = {
                Text("Optional notes like why this video was approved.")
            },
        )

        if (!uiState.isEditMode) {
            ParentSectionCard(
                title = "Save this video",
                subtitle = "Choose whether this stays on its own or joins a playlist.",
            ) {
                SaveTargetChoice(
                    title = "Standalone video",
                    subtitle = "Shows up as its own item on the home screen.",
                    selected = uiState.videoSaveTarget == VideoSaveTarget.STANDALONE,
                    onClick = { viewModel.onVideoSaveTargetSelected(VideoSaveTarget.STANDALONE) },
                )

                SaveTargetChoice(
                    title = "Add to existing playlist",
                    subtitle = if (uiState.availablePlaylists.isEmpty()) {
                        "Create a playlist first, then attach videos here."
                    } else {
                        "Adds this video into one of your saved local playlists."
                    },
                    selected = uiState.videoSaveTarget == VideoSaveTarget.EXISTING_PLAYLIST,
                    enabled = uiState.availablePlaylists.isNotEmpty(),
                    onClick = { viewModel.onVideoSaveTargetSelected(VideoSaveTarget.EXISTING_PLAYLIST) },
                )

                if (uiState.videoSaveTarget == VideoSaveTarget.EXISTING_PLAYLIST &&
                    uiState.availablePlaylists.isNotEmpty()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        uiState.availablePlaylists.forEach { playlist ->
                            PlaylistSelectionCard(
                                option = playlist,
                                selected = playlist.localId == uiState.selectedPlaylistId,
                                onClick = { viewModel.onSelectedPlaylistChanged(playlist.localId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistFormSection(
    uiState: AddMediaUiState,
    viewModel: AddMediaViewModel,
) {
    ParentSectionCard(
        title = "Playlist details",
        subtitle = "Paste a YouTube playlist link or name a local playlist you want to build manually.",
    ) {
        OutlinedTextField(
            value = uiState.playlistUrl,
            onValueChange = viewModel::onPlaylistUrlChanged,
            label = { Text("YouTube playlist URL (optional)") },
            modifier = Modifier.fillMaxWidth(),
            colors = parentTextFieldColors(),
            supportingText = {
                Text("Use this for a direct YouTube playlist. Leave it blank if you are building a local playlist below.")
            },
        )

        OutlinedTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChanged,
            label = { Text("Playlist title") },
            modifier = Modifier.fillMaxWidth(),
            colors = parentTextFieldColors(),
        )

        OutlinedTextField(
            value = uiState.subtitle,
            onValueChange = viewModel::onSubtitleChanged,
            label = { Text("Short subtitle") },
            modifier = Modifier.fillMaxWidth(),
            colors = parentTextFieldColors(),
            supportingText = {
                Text("Example: Bedtime videos or Animal learning mix.")
            },
        )

        OutlinedTextField(
            value = uiState.contextNote,
            onValueChange = viewModel::onContextNoteChanged,
            label = { Text("Parent note") },
            modifier = Modifier.fillMaxWidth(),
            colors = parentTextFieldColors(),
            minLines = 3,
            supportingText = {
                Text("Optional notes like what starts first or what this playlist is for.")
            },
        )
    }

    ParentSectionCard(
        title = "Add videos",
        subtitle = "Optional. Add approved videos here to create a local curated playlist instead of relying on the raw YouTube playlist.",
    ) {
        OutlinedTextField(
            value = uiState.playlistVideoUrl,
            onValueChange = viewModel::onPlaylistVideoUrlChanged,
            label = { Text("YouTube video URL") },
            modifier = Modifier.fillMaxWidth(),
            colors = parentTextFieldColors(),
        )

        ImportStatusCard(
            title = "Video import",
            message = when {
                uiState.isImportingPlaylistVideo -> "Importing video details from YouTube..."
                !uiState.playlistImportMessage.isNullOrBlank() -> uiState.playlistImportMessage
                else -> "Paste a video link to preview it before adding it to this playlist."
            },
        )

        if (uiState.pendingPlaylistVideo != null) {
            PlaylistVideoPreviewCard(
                entry = uiState.pendingPlaylistVideo,
                onAdd = viewModel::addPendingVideoToPlaylist,
            )
        }
    }

    ParentSectionCard(
        title = "Playlist videos",
        subtitle = if (uiState.playlistEntries.isEmpty()) {
            if (uiState.playlistUrl.isBlank()) {
                "No videos added yet."
            } else {
                "No local videos added yet. Child mode can still use the saved YouTube playlist link."
            }
        } else {
            "${uiState.playlistEntries.size} videos ready for this playlist."
        },
    ) {
        if (uiState.playlistEntries.isEmpty()) {
            Text(
                text = "Start by pasting a YouTube video link above.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.playlistEntries.forEachIndexed { index, entry ->
                    PlaylistEntryCard(
                        index = index,
                        entry = entry,
                        onRemove = { viewModel.removePlaylistEntry(entry.localId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportStatusCard(
    title: String,
    message: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SaveTargetChoice(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
            },
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = if (enabled) 0.5f else 0.28f,
            ),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlaylistSelectionCard(
    option: PlaylistOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
            },
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (option.videoCount == 1) "1 video" else "${option.videoCount} videos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected) {
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun PlaylistVideoPreviewCard(
    entry: PlaylistVideoEntry,
    onAdd: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = entry.displayTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            if (entry.displaySubtitle.isNotBlank()) {
                Text(
                    text = entry.displaySubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth(),
                colors = parentSecondaryButtonColors(),
            ) {
                Text("Add To Playlist")
            }
        }
    }
}

@Composable
private fun PlaylistEntryCard(
    index: Int,
    entry: PlaylistVideoEntry,
    onRemove: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f),
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = entry.displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (entry.displaySubtitle.isNotBlank()) {
                    Text(
                        text = entry.displaySubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            TextButton(onClick = onRemove) {
                Text("Remove")
            }
        }
    }
}
