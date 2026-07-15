import CryptoKit
import Foundation

@MainActor
final class ParentPinStore: ObservableObject {
    @Published private(set) var hasPin: Bool

    private let defaults: UserDefaults
    private let pinHashKey = "kidview.parentPinHash"
    private let pinSaltKey = "kidview.parentPinSalt"
    private let hasPinOverride: Bool?

    init(defaults: UserDefaults = .standard, hasPinOverride: Bool? = nil) {
        self.defaults = defaults
        self.hasPinOverride = hasPinOverride

        if let hasPinOverride {
            self.hasPin = hasPinOverride
        } else {
            self.hasPin = defaults.string(forKey: pinHashKey) != nil
        }
    }

    func save(pin: String) -> Bool {
        let trimmed = pin.trimmingCharacters(in: .whitespacesAndNewlines)
        guard trimmed.count >= 4 else {
            return false
        }

        let salt = UUID().uuidString
        let hash = Self.hash(pin: trimmed, salt: salt)

        defaults.set(hash, forKey: pinHashKey)
        defaults.set(salt, forKey: pinSaltKey)
        hasPin = true
        return true
    }

    func validate(pin: String) -> Bool {
        guard let storedHash = defaults.string(forKey: pinHashKey),
              let salt = defaults.string(forKey: pinSaltKey) else {
            return false
        }

        return Self.hash(pin: pin, salt: salt) == storedHash
    }

    private static func hash(pin: String, salt: String) -> String {
        let data = Data((salt + ":" + pin).utf8)
        let digest = SHA256.hash(data: data)
        return digest.map { String(format: "%02x", $0) }.joined()
    }
}
