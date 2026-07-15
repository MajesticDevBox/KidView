import Foundation

@MainActor
final class ApprovedMediaStore: ObservableObject {
    @Published private(set) var items: [ApprovedMediaItem]
    @Published var selectedItemID: String?

    private let defaults: UserDefaults
    private let storageKey = "kidview.approvedMedia"
    private let selectedItemKey = "kidview.selectedItemID"

    init(defaults: UserDefaults = .standard, previewItems: [ApprovedMediaItem]? = nil) {
        self.defaults = defaults

        if let previewItems {
            self.items = previewItems
            self.selectedItemID = previewItems.first?.id
            return
        }

        self.items = []
        self.selectedItemID = defaults.string(forKey: selectedItemKey)
        load()
    }

    var selectedItem: ApprovedMediaItem? {
        guard let selectedItemID else { return nil }
        return items.first(where: { $0.id == selectedItemID })
    }

    func addApprovedMedia(from rawURL: String, title: String = "", note: String = "") -> Bool {
        guard let parsed = YouTubeURLParser.parse(rawURL) else {
            return false
        }

        let newItem = ApprovedMediaItem(
            mediaType: parsed.mediaType,
            youtubeID: parsed.youtubeID,
            originalURL: rawURL,
            displayTitle: title,
            contextNote: note
        )

        items.insert(newItem, at: 0)

        if selectedItemID == nil {
            selectedItemID = newItem.id
        }

        persist()
        return true
    }

    func delete(at offsets: IndexSet) {
        let removingSelected = offsets.contains { index in
            items.indices.contains(index) && items[index].id == selectedItemID
        }

        items.remove(atOffsets: offsets)

        if removingSelected {
            selectedItemID = items.first?.id
        }

        persist()
    }

    func select(_ item: ApprovedMediaItem) {
        selectedItemID = item.id
        persistSelectedItem()
    }

    private func load() {
        guard let data = defaults.data(forKey: storageKey),
              let decoded = try? JSONDecoder().decode([ApprovedMediaItem].self, from: data) else {
            return
        }

        items = decoded

        if selectedItemID == nil || selectedItem == nil {
            selectedItemID = items.first?.id
        }
    }

    private func persist() {
        if let data = try? JSONEncoder().encode(items) {
            defaults.set(data, forKey: storageKey)
        }

        persistSelectedItem()
    }

    private func persistSelectedItem() {
        defaults.set(selectedItemID, forKey: selectedItemKey)
    }
}
