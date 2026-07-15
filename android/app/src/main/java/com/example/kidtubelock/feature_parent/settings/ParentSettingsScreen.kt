package com.example.kidtubelock.feature_parent.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kidtubelock.feature_lock.LockMode
import com.example.kidtubelock.feature_parent.common.InfoNote
import com.example.kidtubelock.feature_parent.common.ParentBottomBar
import com.example.kidtubelock.feature_parent.common.ParentBottomTab
import com.example.kidtubelock.feature_parent.common.ParentScreenBackground
import com.example.kidtubelock.feature_parent.common.ParentSectionCard
import com.example.kidtubelock.feature_parent.common.parentPrimaryButtonColors
import com.example.kidtubelock.feature_parent.common.parentTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentSettingsScreen(
    viewModel: ParentSettingsViewModel,
    onOpenHome: () -> Unit,
    onOpenPlaylist: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTimeLimits: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isPinSheetVisible by rememberSaveable { mutableStateOf(false) }

    if (isPinSheetVisible) {
        ChangeParentPinSheet(
            uiState = uiState,
            onDismiss = { isPinSheetVisible = false },
            onCurrentPinChanged = viewModel::onCurrentPinChanged,
            onNewPinChanged = viewModel::onNewPinChanged,
            onConfirmNewPinChanged = viewModel::onConfirmNewPinChanged,
            onSave = {
                viewModel.updateParentPin {
                    isPinSheetVisible = false
                }
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                ),
                title = { Text("Settings") },
            )
        },
        bottomBar = {
            ParentBottomBar(
                selectedTab = ParentBottomTab.SETTINGS,
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ParentSectionCard(
                    title = "Device setup",
                    subtitle = "Choose how this phone should behave during child mode.",
                ) {
                    Text(
                        text = "Use the standard phone mode for family devices today. The dedicated-device option is still a future-oriented preview branch.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                SettingsNavigationCard(
                    title = "Time limits",
                    description = "Optional only for now.",
                    value = uiState.timeLimitSummary,
                    onClick = onOpenTimeLimits,
                )

                SettingsNavigationCard(
                    title = "Parent PIN",
                    description = uiState.pinSuccessMessage
                        ?: "Update the PIN used for parent controls and child-mode unlock.",
                    value = "Change",
                    onClick = { isPinSheetVisible = true },
                )

                ParentSectionCard(
                    title = "Lock mode",
                    subtitle = "Choose the behavior branch for this device.",
                ) {
                    LockModeOptionRow(
                        title = "Standard phone",
                        description = "Immersive mode plus parent-guided screen pinning.",
                        selected = uiState.preferredLockMode == LockMode.STANDARD_PHONE,
                        onClick = { viewModel.onLockModeSelected(LockMode.STANDARD_PHONE) },
                    )
                    LockModeOptionRow(
                        title = "Dedicated-device preview",
                        description = "Preview branch for future stronger locking behavior.",
                        selected = uiState.preferredLockMode == LockMode.DEDICATED_DEVICE,
                        onClick = { viewModel.onLockModeSelected(LockMode.DEDICATED_DEVICE) },
                    )
                }

                ParentSectionCard(
                    title = "Current guidance",
                ) {
                    Text(
                        text = uiState.guidanceSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeParentPinSheet(
    uiState: ParentSettingsUiState,
    onDismiss: () -> Unit,
    onCurrentPinChanged: (String) -> Unit,
    onNewPinChanged: (String) -> Unit,
    onConfirmNewPinChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Change Parent PIN",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Verify the current PIN first, then save a new one for settings and child-mode unlock.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = uiState.currentPin,
                onValueChange = onCurrentPinChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Current PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = parentTextFieldColors(),
            )

            OutlinedTextField(
                value = uiState.newPin,
                onValueChange = onNewPinChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = parentTextFieldColors(),
            )

            OutlinedTextField(
                value = uiState.confirmNewPin,
                onValueChange = onConfirmNewPinChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm new PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = parentTextFieldColors(),
            )

            InfoNote(
                title = "PIN rules",
                body = "Use 4 to 8 digits. The PIN stays on this phone and is stored as a salted hash.",
            )

            uiState.pinErrorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isUpdatingPin,
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isUpdatingPin,
                    colors = parentPrimaryButtonColors(),
                ) {
                    Text(if (uiState.isUpdatingPin) "Saving..." else "Save PIN")
                }
            }
        }
    }
}

@Composable
private fun SettingsNavigationCard(
    title: String,
    description: String,
    value: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LockModeOptionRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            },
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
