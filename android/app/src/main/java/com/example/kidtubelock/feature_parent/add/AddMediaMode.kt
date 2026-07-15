package com.example.kidtubelock.feature_parent.add

enum class AddMediaMode {
    VIDEO,
    PLAYLIST,
    ;

    companion object {
        fun fromRouteValue(value: String?): AddMediaMode = when (value?.lowercase()) {
            "playlist" -> PLAYLIST
            else -> VIDEO
        }
    }
}
