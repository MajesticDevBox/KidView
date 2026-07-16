package com.mdev.kidview.feature_parent.pin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdev.kidview.domain.repository.ParentControlsRepository
import com.mdev.kidview.domain.security.ParentPinValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class PinSetupUiState(
    val pin: String = "",
    val confirmPin: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class PinSetupViewModel @Inject constructor(
    private val repository: ParentControlsRepository,
) : ViewModel() {
    var uiState by mutableStateOf(PinSetupUiState())
        private set

    fun onPinChanged(value: String) {
        uiState = uiState.copy(
            pin = ParentPinValidator.sanitize(value),
            errorMessage = null,
        )
    }

    fun onConfirmPinChanged(value: String) {
        uiState = uiState.copy(
            confirmPin = ParentPinValidator.sanitize(value),
            errorMessage = null,
        )
    }

    fun savePin(onSuccess: () -> Unit) {
        val pin = uiState.pin
        val confirmPin = uiState.confirmPin

        val error = ParentPinValidator.validateNewPin(
            pin = pin,
            confirmPin = confirmPin,
        )

        if (error != null) {
            uiState = uiState.copy(errorMessage = error)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true, errorMessage = null)
            repository.saveParentPin(pin)
            uiState = uiState.copy(isSaving = false)
            onSuccess()
        }
    }
}
