# Project Roadmap

## Vision

KidView is a private family-use mobile app for playing parent-approved videos in a calm, kid-friendly environment with strong guardrails around what can be watched and how the device is used during viewing.

The product direction is:

- safe, curated video playback
- low-friction parent setup
- strong child-mode confinement
- native Android and iOS apps with a shared brand language

## Brand Direction

Reference artwork:

![KidView brand board](assets/kidview-brand-board.png)

The current brand promise reflected in the artwork is:

- "Safe Videos. Happy Kids."
- playful, friendly, and reassuring
- strong emphasis on safety without looking clinical or restrictive

## Current State

The repository currently contains:

- `android/`: imported Android MVP with Kotlin, Compose, Hilt, DataStore, and tests
- `ios/`: SwiftUI source scaffold ready to be attached to an Xcode project
- `docs/ios/xcode-setup.md`: Xcode bootstrap instructions

The Android app already has:

- parent PIN setup
- approved YouTube link storage
- YouTube URL parsing
- child-mode placeholder flow
- time-limit and lock seams for future expansion

The iOS scaffold already has:

- SwiftUI app shell
- parent PIN setup screen
- approved media list and add flow
- `WKWebView` YouTube player wrapper
- Guided Access status monitoring

## Roadmap Phases

### Phase 1 - Repository Foundation

Goal:

- finish the clean dual-platform repository setup

Deliverables:

- Android source migrated into `android/`
- iOS source scaffold in `ios/`
- project docs and brand direction captured
- Xcode project created on the Mac and committed

Status:

- in progress

### Phase 2 - Android MVP Completion

Goal:

- make the Android family-use version practical for daily testing

Deliverables:

- real child-mode playback with approved media
- reliable parent unlock flow
- polished parent playlist management
- refined timer experience
- app icon and branded Android assets

Success criteria:

- parent can set a PIN, add approved media, and start child mode end-to-end
- app behavior is stable on at least one family phone and one tablet

### Phase 3 - iOS MVP Completion

Goal:

- reach feature parity for core family use on iPhone and iPad

Deliverables:

- Xcode project and simulator build
- parent PIN flow
- approved media management
- child-mode video playback
- Guided Access onboarding and status-aware UI

Success criteria:

- parent can start playback and be guided into Guided Access reliably
- iPhone and iPad layouts both feel intentional

### Phase 4 - Child Lock Hardening

Goal:

- make the app more resilient to accidental exits and misuse

Android work:

- strengthen screen pinning flow
- improve immersive mode handling
- evaluate dedicated-device lock task mode path

iOS work:

- add dedicated Guided Access instruction flow
- reduce child-mode UI escape paths
- move parent PIN material to Keychain

### Phase 5 - Content Experience

Goal:

- make the app easier and faster for a parent to manage

Deliverables:

- playlist naming and organization
- richer metadata and thumbnails
- recent items and favorites
- repeat behavior controls
- optional local-only categories such as bedtime, learning, and songs

### Phase 6 - Polish And Family Testing

Goal:

- turn the private app into something calm, trustworthy, and repeatable for real family use

Deliverables:

- visual refinement to match the artwork
- accessibility pass
- animation and transition polish
- family testing feedback loop
- install guides for Android and iOS

## Milestone Order

1. Create and commit the iOS Xcode project.
2. Finish the Android real playback flow.
3. Finish the iOS real playback flow.
4. Replace temporary unlock shortcuts with parent-authenticated exits.
5. Add branded production assets and screenshots.

## Risks

- YouTube embed behavior may differ between Android `WebView` and iOS `WKWebView`.
- Child lock expectations are stronger than what ordinary consumer devices allow by default.
- The Android app still carries internal `KidTubeLock` naming that should eventually be renamed carefully.
- App-store style art exists, but in-app production assets still need to be exported and applied.

## Near-Term Next Steps

1. Create `ios/KidView.xcodeproj` on the Mac and commit it.
2. Wire the iOS scaffold into the Xcode targets.
3. Decide whether the next implementation push is Android-first or parity-first.
4. Export app icon sizes and splash assets from the brand board.
