package com.example.kidtubelock.feature_player

import android.app.Activity
import android.content.res.Configuration
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kidtubelock.domain.model.ApprovedMediaDisplayFormatter
import com.example.kidtubelock.domain.model.ApprovedMediaItem
import com.example.kidtubelock.domain.youtube.PlaybackAvailability
import com.example.kidtubelock.feature_parent.common.ParentScreenBackground
import com.example.kidtubelock.feature_parent.common.BrandPill
import com.example.kidtubelock.feature_player.playback.YouTubeVideoPlayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChildModeScreen(
    viewModel: ChildModeViewModel,
    onExitToParentHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val item = uiState.item
    val videoIdsToPlay = uiState.videoIdsToPlay
    val sessionPolicy = uiState.sessionPolicy
    val unlockErrorMessage = uiState.unlockErrorMessage
    val canPlay = uiState.playbackAvailability == PlaybackAvailability.PLAYABLE_NOW &&
        videoIdsToPlay.isNotEmpty()
    val sessionTimerText = uiState.timeLimitRemainingLabel?.let { "$it left" }

    BackHandler(enabled = sessionPolicy.absorbBackPress) {
        // Child mode intentionally absorbs back presses until a parent unlock succeeds.
    }

    LaunchedEffect(uiState.exitAuthorized) {
        if (uiState.exitAuthorized) {
            onExitToParentHome()
            viewModel.onExitHandled()
        }
    }

    DisposableEffect(activity, isLandscape, sessionPolicy.keepScreenAwake, sessionPolicy.useImmersiveMode) {
        if (activity != null) {
            val window = activity.window
            if (sessionPolicy.keepScreenAwake) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            if (sessionPolicy.useImmersiveMode && isLandscape) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }

            onDispose {
                controller.show(WindowInsetsCompat.Type.systemBars())
                if (sessionPolicy.keepScreenAwake) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                WindowCompat.setDecorFitsSystemWindows(window, true)
            }
        } else {
            onDispose { }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (isLandscape) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.background
        },
    ) {
        if (isLandscape) {
            if (canPlay) {
                LandscapeViewer(
                    videoIds = videoIdsToPlay,
                    sessionTimerText = sessionTimerText,
                    onParentUnlock = viewModel::openUnlockPrompt,
                )
            } else {
                LandscapeStatusViewer(
                    statusTitle = uiState.playbackStatusLabel,
                    statusDetail = uiState.playbackDetailMessage,
                    sessionTimerText = sessionTimerText,
                    onParentUnlock = viewModel::openUnlockPrompt,
                )
            }
        } else {
            PortraitViewer(
                item = item,
                canPlay = canPlay,
                playbackStatusLabel = uiState.playbackStatusLabel,
                playbackDetailMessage = uiState.playbackDetailMessage,
                parentGuidance = uiState.parentGuidance,
                timeLimitRemainingLabel = uiState.timeLimitRemainingLabel,
                timeLimitReached = uiState.timeLimitReached,
                videoIds = videoIdsToPlay,
                onParentUnlock = viewModel::openUnlockPrompt,
            )
        }
    }

    if (uiState.showUnlockPrompt) {
        ParentUnlockDialog(
            pin = uiState.unlockPin,
            errorMessage = unlockErrorMessage,
            isVerifying = uiState.isVerifyingPin,
            onPinChanged = viewModel::onUnlockPinChanged,
            onVerify = viewModel::verifyParentPin,
            onDismiss = viewModel::dismissUnlockPrompt,
        )
    }
}

@Composable
private fun ParentUnlockDialog(
    pin: String,
    errorMessage: String?,
    isVerifying: Boolean,
    onPinChanged: (String) -> Unit,
    onVerify: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.48f)),
            contentAlignment = Alignment.Center,
        ) {
            ParentScreenBackground {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 26.dp, vertical = 40.dp),
                    shape = RoundedCornerShape(34.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.42f),
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                        ) {
                            UnlockShieldGraphic()

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = "Child Mode is Active",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "To exit, enter\nparent PIN",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            PinDotsRow(
                                pinLength = pin.length,
                                isError = errorMessage != null,
                            )

                            Text(
                                text = if (isVerifying) "Checking parent PIN..." else "Use the keypad below to enter the parent PIN.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            if (!errorMessage.isNullOrBlank()) {
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }

                            PinKeypad(
                                pin = pin,
                                isEnabled = !isVerifying,
                                onDigitPressed = { digit ->
                                    onPinChanged((pin + digit).take(8))
                                },
                                onBackspacePressed = {
                                    if (pin.isNotEmpty()) {
                                        onPinChanged(pin.dropLast(1))
                                    }
                                },
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TextButton(
                                    onClick = onDismiss,
                                    enabled = !isVerifying,
                                ) {
                                    Text("Cancel")
                                }
                                TextButton(
                                    onClick = onVerify,
                                    enabled = !isVerifying,
                                ) {
                                    Text(if (isVerifying) "Checking..." else "Unlock")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinKeypad(
    pin: String,
    isEnabled: Boolean,
    onDigitPressed: (String) -> Unit,
    onBackspacePressed: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
        ).forEach { rowDigits ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowDigits.forEach { digit ->
                    PinKeyButton(
                        modifier = Modifier.weight(1f),
                        label = digit,
                        enabled = isEnabled,
                        onClick = { onDigitPressed(digit) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            PinKeyButton(
                modifier = Modifier.weight(1f),
                label = "0",
                enabled = isEnabled,
                onClick = { onDigitPressed("0") },
            )

            PinKeyButton(
                modifier = Modifier.weight(1f),
                enabled = isEnabled && pin.isNotEmpty(),
                onClick = onBackspacePressed,
                content = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Delete digit",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                },
            )
        }
    }
}

@Composable
private fun PinKeyButton(
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean,
    onClick: () -> Unit = {},
    content: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.84f),
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.34f),
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        if (content != null) {
            content()
        } else if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LandscapeViewer(
    videoIds: List<String>,
    sessionTimerText: String?,
    onParentUnlock: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        YouTubeVideoPlayer(
            videoIds = videoIds,
            modifier = Modifier.fillMaxSize(),
        )

        ParentUnlockChip(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            onParentUnlock = onParentUnlock,
        )

        BrandPill(
            text = sessionTimerText?.let { "$it • Long-press Parent to unlock" }
                ?: "Long-press Parent to unlock",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 20.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LandscapeStatusViewer(
    statusTitle: String,
    statusDetail: String,
    sessionTimerText: String?,
    onParentUnlock: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        ParentScreenBackground {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 36.dp),
                shape = RoundedCornerShape(30.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ParentUnlockChip(
                        onParentUnlock = onParentUnlock,
                    )
                    Text(
                        text = statusTitle.ifBlank { "Playback unavailable" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = statusDetail,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    BrandPill(
                        text = sessionTimerText ?: "Long-press Parent to unlock",
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PortraitViewer(
    item: ApprovedMediaItem?,
    canPlay: Boolean,
    playbackStatusLabel: String,
    playbackDetailMessage: String,
    parentGuidance: String,
    timeLimitRemainingLabel: String?,
    timeLimitReached: Boolean,
    videoIds: List<String>,
    onParentUnlock: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PlayerShellCard(
            item = item,
            canPlay = canPlay,
            playbackStatusLabel = playbackStatusLabel,
            playbackDetailMessage = playbackDetailMessage,
            viewerPillText = when {
                timeLimitReached -> "Time up"
                !timeLimitRemainingLabel.isNullOrBlank() -> "$timeLimitRemainingLabel left"
                else -> "Viewer"
            },
            videoIds = videoIds,
            onParentUnlock = onParentUnlock,
            modifier = Modifier.weight(1f),
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Child Mode is active",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (canPlay) {
                        if (!timeLimitRemainingLabel.isNullOrBlank()) {
                            "Time remaining: $timeLimitRemainingLabel. Rotate to landscape for full-screen viewing. Long-press Parent to unlock."
                        } else {
                            "Rotate to landscape for full-screen viewing. Long-press Parent to unlock."
                        }
                    } else if (timeLimitReached) {
                        "The time limit has been reached. Playback has stopped. Long-press Parent to unlock."
                    } else {
                        parentGuidance.ifBlank { "Long-press Parent to unlock." }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlayerShellCard(
    item: ApprovedMediaItem?,
    canPlay: Boolean,
    playbackStatusLabel: String,
    playbackDetailMessage: String,
    viewerPillText: String,
    videoIds: List<String>,
    onParentUnlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                BrandPill(text = viewerPillText)

                Spacer(modifier = Modifier.weight(1f))

                ParentUnlockChip(
                    onParentUnlock = onParentUnlock,
                )
            }

            if (canPlay && videoIds.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    ),
                ) {
                    YouTubeVideoPlayer(
                        videoIds = videoIds,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(22.dp)),
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = playbackStatusLabel.ifBlank { "Playback unavailable" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = playbackDetailMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = portraitTitle(item),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = portraitSubtitle(item, playbackStatusLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ParentUnlockChip(
    modifier: Modifier = Modifier,
    onParentUnlock: () -> Unit,
) {
    Surface(
        modifier = modifier.combinedClickable(
            onClick = { },
            onLongClick = onParentUnlock,
        ),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.86f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "Parent",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun UnlockShieldGraphic() {
    Surface(
        modifier = Modifier.size(138.dp),
        shape = RoundedCornerShape(
            topStart = 44.dp,
            topEnd = 44.dp,
            bottomEnd = 64.dp,
            bottomStart = 64.dp,
        ),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.92f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.34f),
        ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PinDotsRow(
    pinLength: Int,
    isError: Boolean,
) {
    val slotCount = max(4, pinLength)
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(slotCount) { index ->
            val filled = index < pinLength
            Surface(
                modifier = Modifier.size(16.dp),
                shape = CircleShape,
                color = when {
                    isError && filled -> MaterialTheme.colorScheme.error
                    filled -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (filled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.26f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                    },
                ),
            ) {}
        }
    }
}

private fun portraitTitle(item: ApprovedMediaItem?): String = when (item?.mediaType?.name) {
    null -> "Child Mode"
    else -> ApprovedMediaDisplayFormatter.title(item)
}

private fun portraitSubtitle(
    item: ApprovedMediaItem?,
    playbackStatusLabel: String,
): String = when {
    item == null -> "No approved media is currently available."
    ApprovedMediaDisplayFormatter.context(item).isNotBlank() -> ApprovedMediaDisplayFormatter.context(item)
    else -> ApprovedMediaDisplayFormatter.subtitle(item, playbackStatusLabel)
}
