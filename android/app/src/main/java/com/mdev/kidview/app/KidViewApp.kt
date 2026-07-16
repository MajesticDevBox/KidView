package com.mdev.kidview.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mdev.kidview.feature_parent.add.AddMediaScreen
import com.mdev.kidview.feature_parent.add.AddMediaMode
import com.mdev.kidview.feature_parent.add.AddMediaViewModel
import com.mdev.kidview.feature_parent.handoff.ChildModeHandoffScreen
import com.mdev.kidview.feature_parent.handoff.ChildModeHandoffViewModel
import com.mdev.kidview.feature_parent.home.ParentHomeScreen
import com.mdev.kidview.feature_parent.home.ParentHomeViewModel
import com.mdev.kidview.feature_parent.pin.PinSetupScreen
import com.mdev.kidview.feature_parent.pin.PinSetupViewModel
import com.mdev.kidview.feature_parent.common.ParentBottomTab
import com.mdev.kidview.feature_parent.playlist.ParentPlaylistScreen
import com.mdev.kidview.feature_parent.settings.ParentSettingsScreen
import com.mdev.kidview.feature_parent.settings.ParentSettingsViewModel
import com.mdev.kidview.feature_parent.timelimit.TimeLimitsScreen
import com.mdev.kidview.feature_parent.timelimit.TimeLimitsViewModel
import com.mdev.kidview.feature_player.ChildModeScreen
import com.mdev.kidview.feature_player.ChildModeViewModel

@Composable
fun KidViewApp(
    appEntryViewModel: AppEntryViewModel = hiltViewModel(),
) {
    val launchDestination by appEntryViewModel.launchDestination.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        if (launchDestination == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Surface
        }

        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = launchDestination.orEmpty(),
        ) {
            composable(AppDestination.PinSetup.route) {
                val viewModel: PinSetupViewModel = hiltViewModel()
                PinSetupScreen(
                    viewModel = viewModel,
                    onPinCreated = {
                        navController.navigate(AppDestination.Home.route) {
                            popUpTo(AppDestination.PinSetup.route) {
                                inclusive = true
                            }
                        }
                    },
                )
            }

            composable(AppDestination.Home.route) {
                val viewModel: ParentHomeViewModel = hiltViewModel()
                ParentHomeScreen(
                    viewModel = viewModel,
                    selectedTab = ParentBottomTab.HOME,
                    onAddVideo = {
                        navController.navigate(AppDestination.addMediaRoute(AddMediaMode.VIDEO.name.lowercase()))
                    },
                    onAddPlaylist = {
                        navController.navigate(AppDestination.addMediaRoute(AddMediaMode.PLAYLIST.name.lowercase()))
                    },
                    onOpenHome = { },
                    onOpenPlaylist = { navController.navigate(AppDestination.Playlist.route) },
                    onOpenSettings = { navController.navigate(AppDestination.Settings.route) },
                    onStartChildMode = { itemId ->
                        navController.navigate(AppDestination.childModeHandoffRoute(itemId))
                    },
                )
            }

            composable(AppDestination.Playlist.route) {
                val viewModel: ParentHomeViewModel = hiltViewModel()
                ParentPlaylistScreen(
                    viewModel = viewModel,
                    onAddVideo = {
                        navController.navigate(AppDestination.addMediaRoute(AddMediaMode.VIDEO.name.lowercase()))
                    },
                    onAddPlaylist = {
                        navController.navigate(AppDestination.addMediaRoute(AddMediaMode.PLAYLIST.name.lowercase()))
                    },
                    onEditMedia = { itemId ->
                        navController.navigate(AppDestination.editMediaRoute(itemId))
                    },
                    onOpenHome = {
                        navController.navigate(AppDestination.Home.route) {
                            popUpTo(AppDestination.Home.route)
                            launchSingleTop = true
                        }
                    },
                    onOpenPlaylist = { },
                    onOpenSettings = { navController.navigate(AppDestination.Settings.route) },
                )
            }

            composable(AppDestination.Settings.route) {
                val viewModel: ParentSettingsViewModel = hiltViewModel()
                ParentSettingsScreen(
                    viewModel = viewModel,
                    onOpenHome = {
                        navController.navigate(AppDestination.Home.route) {
                            popUpTo(AppDestination.Home.route)
                            launchSingleTop = true
                        }
                    },
                    onOpenPlaylist = { navController.navigate(AppDestination.Playlist.route) },
                    onOpenSettings = { },
                    onOpenTimeLimits = { navController.navigate(AppDestination.TimeLimits.route) },
                )
            }

            composable(AppDestination.TimeLimits.route) {
                val viewModel: TimeLimitsViewModel = hiltViewModel()
                TimeLimitsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = AppDestination.AddMedia.route,
                arguments = listOf(
                    navArgument(AppDestination.AddMedia.argumentName) {
                        nullable = false
                        type = NavType.StringType
                    },
                ),
            ) {
                val viewModel: AddMediaViewModel = hiltViewModel()
                AddMediaScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onMediaSaved = { navController.popBackStack() },
                )
            }

            composable(
                route = AppDestination.EditMedia.route,
                arguments = listOf(
                    navArgument(AppDestination.EditMedia.argumentName) {
                        nullable = false
                        type = NavType.StringType
                    },
                ),
            ) {
                val viewModel: AddMediaViewModel = hiltViewModel()
                AddMediaScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onMediaSaved = { navController.popBackStack() },
                )
            }

            composable(
                route = AppDestination.ChildModeHandoff.route,
                arguments = listOf(
                    navArgument(AppDestination.ChildModeHandoff.argumentName) {
                        nullable = false
                        type = NavType.StringType
                    },
                ),
            ) {
                val viewModel: ChildModeHandoffViewModel = hiltViewModel()
                ChildModeHandoffScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onContinueToChildMode = { itemId ->
                        navController.navigate(AppDestination.childModeRoute(itemId))
                    },
                )
            }

            composable(
                route = AppDestination.ChildMode.route,
                arguments = listOf(
                    navArgument(AppDestination.ChildMode.argumentName) {
                        nullable = false
                        type = NavType.StringType
                    },
                ),
            ) {
                val viewModel: ChildModeViewModel = hiltViewModel()
                ChildModeScreen(
                    viewModel = viewModel,
                    onExitToParentHome = {
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
