package com.mdev.kidview.feature_player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdev.kidview.app.AppDestination
import com.mdev.kidview.domain.model.ApprovedMediaItem
import com.mdev.kidview.domain.model.ChildModeTimeLimitPolicy
import com.mdev.kidview.domain.repository.ParentControlsRepository
import com.mdev.kidview.domain.model.TimeLimitFormatter
import com.mdev.kidview.domain.youtube.ApprovedMediaPlaybackPolicy
import com.mdev.kidview.domain.youtube.PlaybackSupport
import com.mdev.kidview.domain.youtube.PlaybackAvailability
import com.mdev.kidview.feature_lock.ChildLockControllerResolver
import com.mdev.kidview.feature_lock.ChildSessionPolicy
import com.mdev.kidview.feature_lock.LockMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChildModeUiState(
    val item: ApprovedMediaItem? = null,
    val lockMode: LockMode = LockMode.STANDARD_PHONE,
    val parentGuidance: String = "",
    val sessionPolicy: ChildSessionPolicy = ChildSessionPolicy(),
    val playbackAvailability: PlaybackAvailability = PlaybackAvailability.UNAVAILABLE,
    val playbackStatusLabel: String = "",
    val playbackDetailMessage: String = "",
    val videoIdsToPlay: List<String> = emptyList(),
    val hostedPlaylistIdToPlay: String? = null,
    val timeLimitMinutes: Int? = null,
    val timeLimitRemainingLabel: String? = null,
    val timeLimitReached: Boolean = false,
    val showUnlockPrompt: Boolean = false,
    val unlockPin: String = "",
    val unlockErrorMessage: String? = null,
    val isVerifyingPin: Boolean = false,
    val exitAuthorized: Boolean = false,
)

@HiltViewModel
class ChildModeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ParentControlsRepository,
    private val childLockControllerResolver: ChildLockControllerResolver,
) : ViewModel() {
    private val itemId: String = checkNotNull(
        savedStateHandle[AppDestination.ChildMode.argumentName],
    )
    private val sessionStartedAtEpochMillis = System.currentTimeMillis()

    private val screenState = MutableStateFlow(ChildModeUiState())
    private val currentTimeMillis = MutableStateFlow(sessionStartedAtEpochMillis)

    init {
        viewModelScope.launch {
            while (true) {
                currentTimeMillis.value = System.currentTimeMillis()
                delay(1_000)
            }
        }
    }

    val uiState: StateFlow<ChildModeUiState> = combine(
        repository.approvedMediaItem(itemId),
        repository.appSettings,
        screenState,
        currentTimeMillis,
    ) { item, settings, screenState, nowMillis ->
            val elapsedSeconds = ((nowMillis - sessionStartedAtEpochMillis) / 1_000L).coerceAtLeast(0L)
            val timeLimitStatus = ChildModeTimeLimitPolicy.status(
                limitMinutes = settings.timeLimitMinutes,
                elapsedSeconds = elapsedSeconds,
            )
            val playbackSupport = sessionPlaybackSupport(
                baseSupport = ApprovedMediaPlaybackPolicy.supportFor(item),
                timeLimitStatus = timeLimitStatus,
            )
            val childLockController = childLockControllerResolver.controllerFor(
                settings.preferredLockMode,
            )
            val launchSupport = childLockController.launchSupport()
            val playbackVideoIds = when (item?.mediaType) {
                com.mdev.kidview.domain.model.MediaType.VIDEO -> listOf(item.youtubeId)
                com.mdev.kidview.domain.model.MediaType.PLAYLIST -> item.playlistEntries.map { it.youtubeId }
                null -> emptyList()
            }
            val hostedPlaylistId = item
                ?.takeIf { it.mediaType == com.mdev.kidview.domain.model.MediaType.PLAYLIST }
                ?.takeIf { it.playlistEntries.isEmpty() }
                ?.youtubeId
                ?.takeIf { it.isNotBlank() }
            ChildModeUiState(
                item = item,
                lockMode = childLockController.lockMode,
                parentGuidance = launchSupport.inSessionHint,
                sessionPolicy = childLockController.sessionPolicy(),
                playbackAvailability = playbackSupport.availability,
                playbackStatusLabel = playbackSupport.statusLabel,
                playbackDetailMessage = playbackSupport.detailMessage,
                videoIdsToPlay = playbackVideoIds.takeIf { playbackSupport.isPlayableNow }.orEmpty(),
                hostedPlaylistIdToPlay = hostedPlaylistId.takeIf { playbackSupport.isPlayableNow },
                timeLimitMinutes = settings.timeLimitMinutes,
                timeLimitRemainingLabel = ChildModeTimeLimitPolicy.remainingLabel(
                    timeLimitStatus.remainingSeconds,
                ),
                timeLimitReached = timeLimitStatus.isReached,
                showUnlockPrompt = screenState.showUnlockPrompt,
                unlockPin = screenState.unlockPin,
                unlockErrorMessage = screenState.unlockErrorMessage,
                isVerifyingPin = screenState.isVerifyingPin,
                exitAuthorized = screenState.exitAuthorized,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChildModeUiState(),
        )

    fun openUnlockPrompt() {
        screenState.update {
            it.copy(
                showUnlockPrompt = true,
                unlockPin = "",
                unlockErrorMessage = null,
                isVerifyingPin = false,
            )
        }
    }

    fun dismissUnlockPrompt() {
        screenState.update {
            it.copy(
                showUnlockPrompt = false,
                unlockPin = "",
                unlockErrorMessage = null,
                isVerifyingPin = false,
            )
        }
    }

    fun onUnlockPinChanged(value: String) {
        screenState.update {
            it.copy(
                unlockPin = value.filter(Char::isDigit),
                unlockErrorMessage = null,
            )
        }
    }

    fun verifyParentPin() {
        val pin = screenState.value.unlockPin
        if (pin.isBlank()) {
            screenState.update {
                it.copy(unlockErrorMessage = "Enter the parent PIN.")
            }
            return
        }

        viewModelScope.launch {
            screenState.update {
                it.copy(isVerifyingPin = true, unlockErrorMessage = null)
            }

            val isValid = repository.verifyParentPin(pin)
            if (isValid) {
                screenState.update {
                    it.copy(
                        isVerifyingPin = false,
                        showUnlockPrompt = false,
                        unlockPin = "",
                        exitAuthorized = true,
                    )
                }
            } else {
                screenState.update {
                    it.copy(
                        isVerifyingPin = false,
                        unlockErrorMessage = "Incorrect parent PIN.",
                    )
                }
            }
        }
    }

    fun onExitHandled() {
        screenState.update {
            it.copy(exitAuthorized = false)
        }
    }

    private fun sessionPlaybackSupport(
        baseSupport: PlaybackSupport,
        timeLimitStatus: com.mdev.kidview.domain.model.ChildModeTimeLimitStatus,
    ): PlaybackSupport {
        if (!timeLimitStatus.isReached) {
            return baseSupport
        }

        val limitSummary = TimeLimitFormatter.summary(timeLimitStatus.limitMinutes)
        return PlaybackSupport(
            availability = PlaybackAvailability.UNAVAILABLE,
            isPlayableNow = false,
            statusLabel = "Time limit reached",
            detailMessage = "Playback stopped after $limitSummary. Ask a parent to unlock child mode or start a new session.",
        )
    }
}
