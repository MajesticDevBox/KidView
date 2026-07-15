import Foundation

enum MediaType: String, Codable, CaseIterable, Identifiable {
    case video
    case playlist

    var id: String { rawValue }
}
