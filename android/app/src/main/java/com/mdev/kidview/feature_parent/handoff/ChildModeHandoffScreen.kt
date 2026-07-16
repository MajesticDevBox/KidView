package com.mdev.kidview.feature_parent.handoff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mdev.kidview.domain.model.ApprovedMediaDisplayFormatter
import com.mdev.kidview.feature_parent.common.BrandPill
import com.mdev.kidview.feature_parent.common.ParentScreenBackground
import com.mdev.kidview.feature_parent.common.ParentSectionCard
import com.mdev.kidview.feature_parent.common.StatusBadge
import com.mdev.kidview.feature_parent.common.parentPrimaryButtonColors

private const val SHEET_MEDIA = "media"
private const val SHEET_PREP = "prep"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildModeHandoffScreen(
    viewModel: ChildModeHandoffViewModel,
    onBack: () -> Unit,
    onContinueToChildMode: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val item = uiState.item
    val launchSupport = uiState.launchSupport
    var activeSheet by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                ),
                title = { Text("Handoff") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ParentSectionCard(
                    title = "Ready to hand over",
                    subtitle = "Review the selected item and final setup steps before starting child mode.",
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BrandPill(text = item?.mediaType?.name ?: "Item")
                        StatusBadge(
                            text = uiState.playbackStatusLabel.ifBlank { "Unavailable" },
                            emphasized = uiState.canLaunchChildMode,
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        ParentSectionCard(
                            title = "Selected media",
                            subtitle = if (item == null) {
                                "The selected item is unavailable."
                            } else {
                                "This is what child mode will open."
                            },
                        ) {
                            if (item == null) {
                                Text(
                                    text = "The selected approved item is no longer available.",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            } else {
                                Text(
                                    text = ApprovedMediaDisplayFormatter.title(item),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    text = ApprovedMediaDisplayFormatter.subtitle(
                                        item = item,
                                        playbackStatus = uiState.playbackDetailMessage,
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
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
                                TextButton(
                                    onClick = { activeSheet = SHEET_MEDIA },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("View full media details")
                                }
                            }
                        }
                    }

                    if (launchSupport != null) {
                        item {
                            ParentSectionCard(
                                title = "Before you hand over the phone",
                                subtitle = "Keep the main screen short and open the full checklist only when needed.",
                            ) {
                                Text(
                                    text = "Use screen pinning before starting child mode on a standard phone.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                launchSupport.screenPinningSteps.firstOrNull()?.let { nextStep ->
                                    Card(
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
                                        ),
                                    ) {
                                        Text(
                                            text = "Next: $nextStep",
                                            modifier = Modifier.padding(14.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                                TextButton(
                                    onClick = { activeSheet = SHEET_PREP },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Open full checklist")
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { onContinueToChildMode(viewModel.itemId()) },
                    enabled = uiState.canLaunchChildMode,
                    modifier = Modifier.fillMaxWidth(),
                    colors = parentPrimaryButtonColors(),
                ) {
                    Text("Enter Child Mode")
                }
            }
        }
    }

    if (activeSheet == SHEET_MEDIA && item != null) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            SheetContent(
                title = "Selected media",
                subtitle = "Review the stored link before launch.",
            ) {
                Text(ApprovedMediaDisplayFormatter.title(item))
                Text(ApprovedMediaDisplayFormatter.subtitle(item))
                Text(
                    text = ApprovedMediaDisplayFormatter.context(
                        item = item,
                        fallback = item.originalUrl,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = uiState.playbackDetailMessage,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    if (activeSheet == SHEET_PREP && launchSupport != null) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            SheetContent(
                title = launchSupport.screenPinningTitle,
                subtitle = launchSupport.summary,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    launchSupport.screenPinningSteps.forEachIndexed { index, step ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
                            ),
                        ) {
                            Text(
                                text = "${index + 1}. $step",
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetContent(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}
