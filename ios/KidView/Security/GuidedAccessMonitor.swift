import Foundation
import UIKit

@MainActor
final class GuidedAccessMonitor: ObservableObject {
    @Published private(set) var isGuidedAccessEnabled: Bool

    private var observer: NSObjectProtocol?

    init() {
        isGuidedAccessEnabled = UIAccessibility.isGuidedAccessEnabled

        observer = NotificationCenter.default.addObserver(
            forName: UIAccessibility.guidedAccessStatusDidChangeNotification,
            object: nil,
            queue: .main
        ) { [weak self] _ in
            self?.isGuidedAccessEnabled = UIAccessibility.isGuidedAccessEnabled
        }
    }

    deinit {
        if let observer {
            NotificationCenter.default.removeObserver(observer)
        }
    }
}
