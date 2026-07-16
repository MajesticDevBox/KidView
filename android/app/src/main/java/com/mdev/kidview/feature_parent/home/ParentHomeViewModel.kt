package com.mdev.kidview.feature_parent.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdev.kidview.domain.model.ApprovedMediaItem
import com.mdev.kidview.domain.repository.ParentControlsRepository
import com.mdev.kidview.domain.youtube.ApprovedMediaPlaybackPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ParentHomeUiState(
    val approvedMedia: List<ApprovedMediaItem> = emptyList(),
    val selectedItemId: String? = null,
    val selectedItemPlaybackMessage: String? = null,
) {
    val selectedItem: ApprovedMediaItem?
        get() = approvedMedia.firstOrNull { it.localId == selectedItemId }

    val canStartChildMode: Boolean
        get() = ApprovedMediaPlaybackPolicy.supportFor(selectedItem).isPlayableNow
}

@HiltViewModel
class ParentHomeViewModel @Inject constructor(
    private val repository: ParentControlsRepository,
) : ViewModel() {
    val uiState: StateFlow<ParentHomeUiState> = repository.appSettings
        .map { settings ->
            val selectedItem = settings.approvedMedia.firstOrNull {
                it.localId == settings.selectedApprovedMediaItemId
            }
            val playbackSupport = ApprovedMediaPlaybackPolicy.supportFor(selectedItem)

            ParentHomeUiState(
                approvedMedia = settings.approvedMedia,
                selectedItemId = settings.selectedApprovedMediaItemId,
                selectedItemPlaybackMessage = if (selectedItem != null) {
                    playbackSupport.detailMessage
                } else {
                    null
                },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ParentHomeUiState(),
        )

    fun onApprovedMediaSelected(itemId: String) {
        viewModelScope.launch {
            repository.selectApprovedMedia(itemId)
        }
    }

    fun deleteApprovedMedia(itemId: String) {
        viewModelScope.launch {
            repository.deleteApprovedMedia(itemId)
        }
    }
}
