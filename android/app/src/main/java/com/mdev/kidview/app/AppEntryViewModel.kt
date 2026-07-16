package com.mdev.kidview.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdev.kidview.domain.repository.ParentControlsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppEntryViewModel @Inject constructor(
    repository: ParentControlsRepository,
) : ViewModel() {
    val launchDestination = repository.appSettings
        .map { settings ->
            if (settings.hasParentPin) {
                AppDestination.Home.route
            } else {
                AppDestination.PinSetup.route
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
}

