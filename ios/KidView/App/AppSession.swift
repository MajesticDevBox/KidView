import Foundation

@MainActor
final class AppSession: ObservableObject {
    @Published var activeMedia: ApprovedMediaItem?
    @Published var isChildModePresented = false

    func startChildMode(with media: ApprovedMediaItem) {
        activeMedia = media
        isChildModePresented = true
    }

    func stopChildMode() {
        isChildModePresented = false
        activeMedia = nil
    }
}
