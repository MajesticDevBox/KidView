package com.example.kidtubelock.domain.model

object ApprovedMediaSelectionResolver {
    fun resolveSelectedItemId(
        approvedMedia: List<ApprovedMediaItem>,
        requestedSelectedItemId: String?,
    ): String? {
        if (approvedMedia.isEmpty()) {
            return null
        }

        return approvedMedia.firstOrNull { it.localId == requestedSelectedItemId }?.localId
            ?: approvedMedia.first().localId
    }
}
