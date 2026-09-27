<p align="center">
  <img src="docs/assets/kidview-brand-board.png" alt="KidView brand board" width="640">
</p>

<h1 align="center">KidView</h1>

<p align="center"><strong>Safe Videos. Happy Kids.</strong></p>

<p align="center">
  A distraction-free, parent-approved video player for kids — built native for Android and iOS.
</p>

<p align="center">
  <img alt="Android" src="https://img.shields.io/badge/Android-Kotlin%20%2B%20Compose-1E88E5?logo=android&logoColor=white">
  <img alt="iOS" src="https://img.shields.io/badge/iOS-SwiftUI-0D47A1?logo=apple&logoColor=white">
  <img alt="Status" src="https://img.shields.io/badge/status-active%20development-FFD54F">
  <img alt="License" src="https://img.shields.io/badge/license-All%20Rights%20Reserved-4CAF50">
</p>

---

## What is KidView

KidView lets a parent build a small, hand-picked library of YouTube videos and playlists, then hand the device to a child inside a locked-down player — no search, no recommendations, no accidental exits into the wider app or browser. It's local-first and private: everything a family sets up lives on the device, with no accounts, tracking, or analytics.

- **Parent mode** — add approved videos/playlists by URL, manage settings, protected by a PIN.
- **Child mode** — a full-screen, distraction-free player locked to only what's been approved.
- **Device-level lock** — Guided Access (iOS) / screen pinning (Android) keeps kids from backing out.

This repository is a clean, dual-platform rebuild: `android/` carries forward the original Android MVP, and `ios/` is a parallel native SwiftUI scaffold for the upcoming iPhone/iPad app.

## Status

| Platform | State |
|---|---|
| **Android** | MVP implemented — Kotlin, Jetpack Compose, Hilt, DataStore (`android/`) |
| **iOS** | Source scaffold in place, Xcode project not yet generated (`ios/`) |

See [`docs/Project Roadmap.md`](docs/Project%20Roadmap.md) for the full phased plan and near-term priorities.

## Repository layout

```
KidView/
├── android/   Android Studio project (Kotlin, Compose, Hilt, DataStore)
├── ios/       SwiftUI source scaffold for the future native iOS app
└── docs/      Product, design, security, and setup documentation
```

## Getting started

| I want to... | Start here |
|---|---|
| Build the Android app | [`docs/android-studio-setup.md`](docs/android-studio-setup.md) |
| Set up the iOS Xcode project | [`docs/ios/xcode-setup.md`](docs/ios/xcode-setup.md) |
| Understand the domain model & data contracts | [`docs/API Documentation.md`](docs/API%20Documentation.md) |
| Review the visual/brand direction | [`docs/UI Design Guide.md`](docs/UI%20Design%20Guide.md) |
| See where the project is headed | [`docs/Project Roadmap.md`](docs/Project%20Roadmap.md) |

> **Note:** Open `android/` directly in Android Studio, not the repository root — see the setup doc above for why.

**Android:** Kotlin · Jetpack Compose · Hilt · DataStore · min SDK 26
**iOS:** Swift · SwiftUI · iOS 17+ target

## Security & privacy

KidView is built around a simple rule: **kids never leave the approved list, and the app never phones home.**

- No analytics, no third-party SDKs, no accounts.
- Parent PIN is stored as a hash, never in plaintext.
- All approved content lives locally on-device.

Full threat model and hardening notes live in [`docs/Security.md`](docs/Security.md). Found a vulnerability? See [`SECURITY.md`](SECURITY.md) for how to report it privately.

## Import notes

The Android source was imported from an earlier local prototype (`KidTubeLock`). Source, tests, and Gradle configuration were carried forward as-is; build output, local machine settings, IDE state, and signing keys were intentionally left behind. Git history from the original folder could not be preserved, since it predated this repository.

## License

**All rights reserved.** This repository is public for reference and portfolio purposes only — see [`LICENSE`](LICENSE). No reuse, redistribution, or derivative use is permitted without prior written permission.
