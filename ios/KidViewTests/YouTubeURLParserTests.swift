import XCTest
@testable import KidView

final class YouTubeURLParserTests: XCTestCase {
    func testParsesWatchVideoURL() {
        let parsed = YouTubeURLParser.parse("https://www.youtube.com/watch?v=abc123")

        XCTAssertEqual(parsed, ParsedYouTubeMedia(mediaType: .video, youtubeID: "abc123"))
    }

    func testParsesWatchPlaylistURL() {
        let parsed = YouTubeURLParser.parse("https://www.youtube.com/watch?list=PL123")

        XCTAssertEqual(parsed, ParsedYouTubeMedia(mediaType: .playlist, youtubeID: "PL123"))
    }

    func testParsesShortURL() {
        let parsed = YouTubeURLParser.parse("https://youtu.be/xyz999")

        XCTAssertEqual(parsed, ParsedYouTubeMedia(mediaType: .video, youtubeID: "xyz999"))
    }

    func testRejectsUnsupportedHost() {
        XCTAssertNil(YouTubeURLParser.parse("https://example.com/watch?v=abc123"))
    }
}
