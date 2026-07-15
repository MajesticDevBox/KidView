package com.example.kidtubelock.feature_parent.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kidtubelock.domain.model.ApprovedMediaDisplayFormatter
import com.example.kidtubelock.domain.model.ApprovedMediaItem
import com.example.kidtubelock.domain.youtube.ApprovedMediaPlaybackPolicy
import com.example.kidtubelock.feature_parent.common.ParentScreenBackground
import com.example.kidtubelock.feature_parent.common.ParentBottomBar
import com.example.kidtubelock.feature_parent.common.ParentBottomTab
import com.example.kidtubelock.feature_parent.common.StatusBadge
import com.example.kidtubelock.feature_parent.common.parentPrimaryButtonColors
import com.example.kidtubelock.feature_parent.common.parentSecondaryButtonColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentHomeScreen(
    viewModel: ParentHomeViewModel,
    selectedTab: ParentBottomTab,
    onAddVideo: () -> Unit,
    onAddPlaylist: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenPlaylist: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartChildMode: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedItem = uiState.selectedItem

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                ),
                title = {
                    Text(
                        text = "Parent Mode",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
        bottomBar = {
            ParentBottomBar(
                selectedTab = selectedTab,
                onHome = onOpenHome,
                onPlaylist = onOpenPlaylist,
                onSettings = onOpenSettings,
            )
        },
    ) { paddingValues ->
        ParentScreenBackground(
            modifier = Modifier.padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "My Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                if (uiState.approvedMedia.isEmpty()) {
                    EmptyPlaylistCard(
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        itemsIndexed(uiState.approvedMedia, key = { _, item -> item.localId }) { index, item ->
                            ApprovedMediaCard(
                                item = item,
                                index = index,
                                selected = item.localId == selectedItem?.localId,
                                playbackStatus = ApprovedMediaPlaybackPolicy.supportFor(item).statusLabel,
                                selectedItemPlaybackMessage = if (item.localId == selectedItem?.localId) {
                                    uiState.selectedItemPlaybackMessage
                                } else {
                                    null
                                },
                                onClick = { viewModel.onApprovedMediaSelected(item.localId) },
                                onDelete = { viewModel.deleteApprovedMedia(item.localId) },
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onAddVideo,
                        modifier = Modifier.weight(1f),
                        colors = parentSecondaryButtonColors(),
                    ) {
                        Text("Add Video")
                    }

                    Button(
                        onClick = onAddPlaylist,
                        modifier = Modifier.weight(1f),
                        colors = parentSecondaryButtonColors(),
                    ) {
                        Text("Add Playlist")
                    }
                }

                Button(
                    onClick = {
                        selectedItem?.localId?.let(onStartChildMode)
                    },
                    enabled = uiState.canStartChildMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    colors = parentPrimaryButtonColors(),
                ) {
                    Text("Start Child Mode")
                }
            }
        }
    }
}

@Composable
private fun EmptyPlaylistCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "No approved media yet",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Add a video or build a local playlist to create a safe watch list.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ApprovedMediaCard(
    item: ApprovedMediaItem,
    index: Int,
    selected: Boolean,
    playbackStatus: String,
    selectedItemPlaybackMessage: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            },
        ),
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
            MediaThumb(
                label = item.mediaType.name,
                index = index,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = ApprovedMediaDisplayFormatter.title(item, index),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (selected) {
                        ApprovedMediaDisplayFormatter.context(
                            item = item,
                            fallback = selectedItemPlaybackMessage ?: "Selected for child mode",
                        )
                    } else {
                        ApprovedMediaDisplayFormatter.subtitle(item, playbackStatus)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (selected) {
                    StatusBadge(
                        text = "Selected",
                        emphasized = true,
                    )
                }
                TextButton(
                    onClick = onDelete,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun MediaThumb(
    label: String,
    index: Int,
) {
    val shortLabel = if (label == "VIDEO") "VID" else "LIST"
    Card(
        modifier = Modifier.size(width = 64.dp, height = 52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f),
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = shortLabel,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
