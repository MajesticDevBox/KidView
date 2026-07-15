package com.example.kidtubelock.feature_parent.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtubelock.domain.model.TimeLimitFormatter
import com.example.kidtubelock.domain.repository.ParentControlsRepository
import com.example.kidtubelock.domain.security.ParentPinValidator
import com.example.kidtubelock.feature_lock.ChildLockControllerResolver
import com.example.kidtubelock.feature_lock.LockMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class PinChangeFormState(
    val currentPin: String = "",
    val newPin: String = "",
    val confirmNewPin: String = "",
    val isUpdatingPin: Boolean = false,
    val pinErrorMessage: String? = null,
    val pinSuccessMessage: String? = null,
)

data class ParentSettingsUiState(
    val preferredLockMode: LockMode = LockMode.STANDARD_PHONE,
    val guidanceSummary: String = "",
    val timeLimitSummary: String = "Off",
    val currentPin: String = "",
    val newPin: String = "",
    val confirmNewPin: String = "",
    val isUpdatingPin: Boolean = false,
    val pinErrorMessage: String? = null,
    val pinSuccessMessage: String? = null,
)

@HiltViewModel
class ParentSettingsViewModel @Inject constructor(
    private val repository: ParentControlsRepository,
    private val childLockControllerResolver: ChildLockControllerResolver,
) : ViewModel() {
    private val pinChangeFormState = MutableStateFlow(PinChangeFormState())

    val uiState: StateFlow<ParentSettingsUiState> = combine(
        repository.appSettings,
        pinChangeFormState,
    ) { settings, pinForm ->
            val controller = childLockControllerResolver.controllerFor(settings.preferredLockMode)
            ParentSettingsUiState(
                preferredLockMode = settings.preferredLockMode,
                guidanceSummary = controller.launchSupport().summary,
                timeLimitSummary = TimeLimitFormatter.summary(settings.timeLimitMinutes),
                currentPin = pinForm.currentPin,
                newPin = pinForm.newPin,
                confirmNewPin = pinForm.confirmNewPin,
                isUpdatingPin = pinForm.isUpdatingPin,
                pinErrorMessage = pinForm.pinErrorMessage,
                pinSuccessMessage = pinForm.pinSuccessMessage,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ParentSettingsUiState(),
        )

    fun onLockModeSelected(lockMode: LockMode) {
        viewModelScope.launch {
            repository.setPreferredLockMode(lockMode)
        }
    }

    fun onCurrentPinChanged(value: String) {
        pinChangeFormState.update { currentState ->
            currentState.copy(
                currentPin = ParentPinValidator.sanitize(value),
                pinErrorMessage = null,
                pinSuccessMessage = null,
            )
        }
    }

    fun onNewPinChanged(value: String) {
        pinChangeFormState.update { currentState ->
            currentState.copy(
                newPin = ParentPinValidator.sanitize(value),
                pinErrorMessage = null,
                pinSuccessMessage = null,
            )
        }
    }

    fun onConfirmNewPinChanged(value: String) {
        pinChangeFormState.update { currentState ->
            currentState.copy(
                confirmNewPin = ParentPinValidator.sanitize(value),
                pinErrorMessage = null,
                pinSuccessMessage = null,
            )
        }
    }

    fun updateParentPin(onSuccess: () -> Unit) {
        val formState = pinChangeFormState.value
        val validationError = ParentPinValidator.validatePinChange(
            currentPin = formState.currentPin,
            newPin = formState.newPin,
            confirmNewPin = formState.confirmNewPin,
        )

        if (validationError != null) {
            pinChangeFormState.update { currentState ->
                currentState.copy(
                    pinErrorMessage = validationError,
                    pinSuccessMessage = null,
                )
            }
            return
        }

        viewModelScope.launch {
            pinChangeFormState.update { currentState ->
                currentState.copy(
                    isUpdatingPin = true,
                    pinErrorMessage = null,
                    pinSuccessMessage = null,
                )
            }

            val isCurrentPinValid = repository.verifyParentPin(formState.currentPin)
            if (!isCurrentPinValid) {
                pinChangeFormState.update { currentState ->
                    currentState.copy(
                        isUpdatingPin = false,
                        pinErrorMessage = "Current parent PIN is incorrect.",
                        pinSuccessMessage = null,
                    )
                }
                return@launch
            }

            repository.saveParentPin(formState.newPin)
            pinChangeFormState.value = PinChangeFormState(
                pinSuccessMessage = "Parent PIN updated.",
            )
            onSuccess()
        }
    }
}
