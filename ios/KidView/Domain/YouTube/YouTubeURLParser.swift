import Foundation

struct ParsedYouTubeMedia: Equatable {
    let mediaType: MediaType
    let youtubeID: String
}

enum YouTubeURLParser {
    private static let supportedHosts: Set<String> = [
        "youtube.com",
        "www.youtube.com",
        "m.youtube.com",
        "youtu.be"
    ]

    static func parse(_ rawURL: String) -> ParsedYouTubeMedia? {
        let trimmed = rawURL.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty,
              let components = URLComponents(string: trimmed),
              let host = components.host?.lowercased(),
              supportedHosts.contains(host) else {
            return nil
        }

        let queryParameters = Dictionary(
            uniqueKeysWithValues: (components.queryItems ?? []).map { item in
                (item.name, item.value ?? "")
            }
        )

        let pathSegments = components.path
            .split(separator: "/")
            .map(String.init)

        if host == "youtu.be" {
            guard let id = pathSegments.first, !id.isEmpty else { return nil }
            return ParsedYouTubeMedia(mediaType: .video, youtubeID: id)
        }

        if components.path == "/watch" {
            if let videoID = queryParameters["v"], !videoID.isEmpty {
                return ParsedYouTubeMedia(mediaType: .video, youtubeID: videoID)
            }

            if let playlistID = queryParameters["list"], !playlistID.isEmpty {
                return ParsedYouTubeMedia(mediaType: .playlist, youtubeID: playlistID)
            }

            return nil
        }

        if components.path == "/playlist" {
            guard let playlistID = queryParameters["list"], !playlistID.isEmpty else {
                return nil
            }

            return ParsedYouTubeMedia(mediaType: .playlist, youtubeID: playlistID)
        }

        if let firstSegment = pathSegments.first,
           ["shorts", "embed", "live"].contains(firstSegment) {
            guard pathSegments.count > 1 else { return nil }
            return ParsedYouTubeMedia(mediaType: .video, youtubeID: pathSegments[1])
        }

        return nil
    }
}
