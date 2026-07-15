package com.example.kidtubelock.feature_parent.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kidtubelock.feature_parent.common.ParentBrandHeader
import com.example.kidtubelock.feature_parent.common.ParentHeroCard
import com.example.kidtubelock.feature_parent.common.ParentScreenBackground
import com.example.kidtubelock.feature_parent.common.ParentSectionCard
import com.example.kidtubelock.feature_parent.common.parentPrimaryButtonColors
import com.example.kidtubelock.feature_parent.common.parentTextFieldColors

@Composable
fun PinSetupScreen(
    viewModel: PinSetupViewModel,
    onPinCreated: () -> Unit,
) {
    val uiState = viewModel.uiState

    Surface(modifier = Modifier.fillMaxSize()) {
        ParentScreenBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                ParentBrandHeader(subtitle = "Private family setup")

                ParentHeroCard(
                    eyebrow = "Private family setup",
                    title = "Create a parent PIN",
                    body = "This PIN protects the parent controls and the unlock path from child mode.",
                )

                ParentSectionCard(
                    title = "PIN setup",
                    subtitle = "Use at least four digits.",
                ) {
                    OutlinedTextField(
                        value = uiState.pin,
                        onValueChange = viewModel::onPinChanged,
                        label = { Text("Parent PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = parentTextFieldColors(),
                    )

                    OutlinedTextField(
                        value = uiState.confirmPin,
                        onValueChange = viewModel::onConfirmPinChanged,
                        label = { Text("Confirm PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = parentTextFieldColors(),
                    )

                    if (uiState.errorMessage != null) {
                        Card {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.savePin(onPinCreated) },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        colors = parentPrimaryButtonColors(),
                    ) {
                        Text(if (uiState.isSaving) "Saving..." else "Save Parent PIN")
                    }
                }
            }
        }
    }
}
