import Foundation

enum PreviewData {
    static let sampleMedia: [ApprovedMediaItem] = [
        ApprovedMediaItem(
            mediaType: .video,
            youtubeID: "dQw4w9WgXcQ",
            originalURL: "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            displayTitle: "Sample Video"
        ),
        ApprovedMediaItem(
            mediaType: .playlist,
            youtubeID: "PL1234567890ABCDEF",
            originalURL: "https://www.youtube.com/playlist?list=PL1234567890ABCDEF",
            displayTitle: "Sample Playlist"
        )
    ]
}
