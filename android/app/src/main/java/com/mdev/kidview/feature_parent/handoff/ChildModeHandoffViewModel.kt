package com.mdev.kidview.feature_parent.handoff

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdev.kidview.app.AppDestination
import com.mdev.kidview.domain.model.ApprovedMediaItem
import com.mdev.kidview.domain.repository.ParentControlsRepository
import com.mdev.kidview.domain.youtube.ApprovedMediaPlaybackPolicy
import com.mdev.kidview.feature_lock.ChildLockControllerResolver
import com.mdev.kidview.feature_lock.ChildLockLaunchSupport
import com.mdev.kidview.feature_lock.LockMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class ChildModeHandoffUiState(
    val item: ApprovedMediaItem? = null,
    val playbackStatusLabel: String = "",
    val playbackDetailMessage: String = "",
    val canLaunchChildMode: Boolean = false,
    val launchSupport: ChildLockLaunchSupport? = null,
    val lockMode: LockMode = LockMode.STANDARD_PHONE,
)

@HiltViewModel
class ChildModeHandoffViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: ParentControlsRepository,
    childLockControllerResolver: ChildLockControllerResolver,
) : ViewModel() {
    private val itemId: String = checkNotNull(
        savedStateHandle[AppDestination.ChildModeHandoff.argumentName],
    )

    val uiState: StateFlow<ChildModeHandoffUiState> = combine(
        repository.approvedMediaItem(itemId),
        repository.appSettings,
    ) { item, settings ->
            val playbackSupport = ApprovedMediaPlaybackPolicy.supportFor(item)
            val childLockController = childLockControllerResolver.controllerFor(
                settings.preferredLockMode,
            )
            ChildModeHandoffUiState(
                item = item,
                playbackStatusLabel = playbackSupport.statusLabel,
                playbackDetailMessage = playbackSupport.detailMessage,
                canLaunchChildMode = playbackSupport.isPlayableNow,
                launchSupport = childLockController.launchSupport(),
                lockMode = childLockController.lockMode,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChildModeHandoffUiState(),
        )

    fun itemId(): String = itemId
}
