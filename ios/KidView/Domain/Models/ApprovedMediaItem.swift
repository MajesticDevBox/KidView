import Foundation

struct ApprovedMediaItem: Codable, Identifiable, Equatable {
    let id: String
    let mediaType: MediaType
    let youtubeID: String
    let originalURL: String
    var displayTitle: String
    var displaySubtitle: String
    var contextNote: String
    var expectedItemCount: Int?
    var thumbnailURL: String
    var playlistEntries: [PlaylistVideoEntry]
    let addedAt: Date

    init(
        id: String = UUID().uuidString,
        mediaType: MediaType,
        youtubeID: String,
        originalURL: String,
        displayTitle: String = "",
        displaySubtitle: String = "",
        contextNote: String = "",
        expectedItemCount: Int? = nil,
        thumbnailURL: String = "",
        playlistEntries: [PlaylistVideoEntry] = [],
        addedAt: Date = Date()
    ) {
        self.id = id
        self.mediaType = mediaType
        self.youtubeID = youtubeID
        self.originalURL = originalURL
        self.displayTitle = displayTitle
        self.displaySubtitle = displaySubtitle
        self.contextNote = contextNote
        self.expectedItemCount = expectedItemCount
        self.thumbnailURL = thumbnailURL
        self.playlistEntries = playlistEntries
        self.addedAt = addedAt
    }

    var fallbackTitle: String {
        if !displayTitle.isEmpty {
            return displayTitle
        }

        switch mediaType {
        case .video:
            return "YouTube Video"
        case .playlist:
            return "YouTube Playlist"
        }
    }
}

struct PlaylistVideoEntry: Codable, Identifiable, Equatable {
    let id: String
    let youtubeID: String
    let originalURL: String
    var displayTitle: String
    var displaySubtitle: String
    var thumbnailURL: String
    let addedAt: Date

    init(
        id: String = UUID().uuidString,
        youtubeID: String,
        originalURL: String,
        displayTitle: String = "",
        displaySubtitle: String = "",
        thumbnailURL: String = "",
        addedAt: Date = Date()
    ) {
        self.id = id
        self.youtubeID = youtubeID
        self.originalURL = originalURL
        self.displayTitle = displayTitle
        self.displaySubtitle = displaySubtitle
        self.thumbnailURL = thumbnailURL
        self.addedAt = addedAt
    }
}
