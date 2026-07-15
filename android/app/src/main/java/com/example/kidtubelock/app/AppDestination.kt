package com.example.kidtubelock.app

sealed class AppDestination(val route: String) {
    data object PinSetup : AppDestination("pin_setup")
    data object Home : AppDestination("home")
    data object Playlist : AppDestination("playlist")
    data object Settings : AppDestination("settings")
    data object TimeLimits : AppDestination("time_limits")
    data object AddMedia : AppDestination("add_media/{mode}") {
        const val argumentName = "mode"
    }
    data object EditMedia : AppDestination("edit_media/{itemId}") {
        const val argumentName = "itemId"
    }
    data object ChildModeHandoff : AppDestination("child_mode_handoff/{itemId}") {
        const val argumentName = "itemId"
    }

    data object ChildMode : AppDestination("child_mode/{itemId}") {
        const val argumentName = "itemId"
    }

    companion object {
        fun addMediaRoute(mode: String): String = "add_media/$mode"

        fun childModeHandoffRoute(itemId: String): String = "child_mode_handoff/$itemId"

        fun childModeRoute(itemId: String): String = "child_mode/$itemId"

        fun editMediaRoute(itemId: String): String = "edit_media/$itemId"
    }
}
