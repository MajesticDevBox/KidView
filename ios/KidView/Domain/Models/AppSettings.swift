import Foundation

struct AppSettings: Codable {
    var hasParentPin: Bool = false
    var approvedMedia: [ApprovedMediaItem] = []
    var selectedApprovedMediaItemID: String?
    var timeLimitMinutes: Int?
}
