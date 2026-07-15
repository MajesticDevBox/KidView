package com.example.kidtubelock.feature_parent.playlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.kidtubelock.feature_parent.common.ParentBottomBar
import com.example.kidtubelock.feature_parent.common.ParentBottomTab
import com.example.kidtubelock.feature_parent.common.ParentScreenBackground
import com.example.kidtubelock.feature_parent.common.StatusBadge
import com.example.kidtubelock.feature_parent.common.parentPrimaryButtonColors
import com.example.kidtubelock.feature_parent.common.parentSecondaryButtonColors
import com.example.kidtubelock.feature_parent.home.ParentHomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPlaylistScreen(
    viewModel: ParentHomeViewModel,
    onAddVideo: () -> Unit,
    onAddPlaylist: () -> Unit,
    onEditMedia: (String) -> Unit,
    onOpenHome: () -> Unit,
    onOpenPlaylist: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                ),
                title = {
                    Text(
                        text = "My Playlist",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
        bottomBar = {
            ParentBottomBar(
                selectedTab = ParentBottomTab.PLAYLIST,
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
                    text = "Review, edit, and curate approved links.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (uiState.approvedMedia.isEmpty()) {
                    PlaylistEmptyState(
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(uiState.approvedMedia, key = { it.localId }) { item ->
                            PlaylistManagementCard(
                                item = item,
                                selected = item.localId == uiState.selectedItemId,
                                onSelect = { viewModel.onApprovedMediaSelected(item.localId) },
                                onEdit = { onEditMedia(item.localId) },
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
            }
        }
    }
}

@Composable
private fun PlaylistEmptyState(
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
                text = "No playlist items yet",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Add approved YouTube links, then come back here to edit or reorder your safe list later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlaylistManagementCard(
    item: ApprovedMediaItem,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlaylistThumb(label = item.mediaType.name)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = ApprovedMediaDisplayFormatter.title(item),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = ApprovedMediaDisplayFormatter.subtitle(item),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (selected) {
                    StatusBadge(
                        text = "Selected",
                        emphasized = true,
                    )
                }
            }

            Text(
                text = ApprovedMediaDisplayFormatter.context(
                    item = item,
                    fallback = item.originalUrl,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = parentSecondaryButtonColors(),
                    contentPadding = PaddingValues(vertical = 10.dp),
                ) {
                    Text("Edit")
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = parentPrimaryButtonColors(),
                    contentPadding = PaddingValues(vertical = 10.dp),
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun PlaylistThumb(
    label: String,
) {
    Card(
        modifier = Modifier.size(width = 64.dp, height = 52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (label == "PLAYLIST") "LIST" else "VID",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
