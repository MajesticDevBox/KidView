package com.example.kidtubelock.feature_parent.timelimit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtubelock.domain.model.TimeLimitFormatter
import com.example.kidtubelock.domain.repository.ParentControlsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimeLimitsUiState(
    val savedMinutes: Int? = null,
    val selectedMinutes: Int? = null,
    val isSaving: Boolean = false,
    val hasChanges: Boolean = false,
    val summaryLabel: String = "Off",
    val timerFaceLabel: String = "--:--",
    val endAtLabel: String = "No end time set",
)

@HiltViewModel
class TimeLimitsViewModel @Inject constructor(
    private val repository: ParentControlsRepository,
) : ViewModel() {
    private val draftMinutes = MutableStateFlow<Int?>(null)
    private val hasDraftSelection = MutableStateFlow(false)
    private val isSaving = MutableStateFlow(false)

    val uiState: StateFlow<TimeLimitsUiState> = combine(
        repository.appSettings,
        draftMinutes,
        hasDraftSelection,
        isSaving,
    ) { settings, draftMinutes, hasDraftSelection, isSaving ->
            val selectedMinutes = if (hasDraftSelection) {
                draftMinutes
            } else {
                settings.timeLimitMinutes
            }
            TimeLimitsUiState(
                savedMinutes = settings.timeLimitMinutes,
                selectedMinutes = selectedMinutes,
                isSaving = isSaving,
                hasChanges = selectedMinutes != settings.timeLimitMinutes,
                summaryLabel = TimeLimitFormatter.summary(selectedMinutes),
                timerFaceLabel = TimeLimitFormatter.timerFace(selectedMinutes),
                endAtLabel = TimeLimitFormatter.endAtLabel(selectedMinutes),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TimeLimitsUiState(),
        )

    fun onTimeLimitSelected(minutes: Int?) {
        draftMinutes.value = minutes
        hasDraftSelection.value = true
    }

    fun saveTimeLimit() {
        val minutes = uiState.value.selectedMinutes
        viewModelScope.launch {
            isSaving.value = true
            repository.setTimeLimitMinutes(minutes)
            hasDraftSelection.value = false
            draftMinutes.value = minutes
            isSaving.value = false
        }
    }
}
