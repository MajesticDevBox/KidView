# Codex

## Purpose

This file helps future Codex sessions work consistently inside the KidView repository.

Use it as the project-specific operating guide for:

- repo layout
- coding expectations
- naming direction
- safe next tasks

## Repository Layout

- `android/`: Android Studio project for the current Kotlin app
- `ios/`: SwiftUI source scaffold and future Xcode project
- `docs/`: product, design, security, and setup docs

Important docs:

- `docs/Project Roadmap.md`
- `docs/UI Design Guide.md`
- `docs/API Documentation.md`
- `docs/Security.md`
- `docs/ios/xcode-setup.md`

## Current Project Reality

The Android codebase came from a local app snapshot that still uses `KidTubeLock` internally in package names and some app identifiers.

The repository brand and future product name are now:

- `KidView`

Codex should preserve behavior first and rename internal identifiers only in deliberate, scoped passes.

## Preferred Working Style

When editing this repo:

- make additive, low-risk changes by default
- avoid broad renames unless the task explicitly asks for them
- leave signing files and local machine config out of version control
- verify Android changes with Gradle tests when possible
- document iOS steps clearly when Windows cannot run Xcode

## Platform Guidance

### Android

Use:

- Kotlin
- Jetpack Compose
- Material 3
- Hilt
- DataStore

Priorities:

- finish real playback flow
- strengthen child-mode entry and exit
- improve curated media management

### iOS

Use:

- Swift
- SwiftUI
- `WKWebView`
- Keychain for final PIN storage

Priorities:

- create and commit `KidView.xcodeproj`
- attach the scaffold files to the project
- replace temporary storage and exit shortcuts

## Design Guidance

Use the KidView brand board in `docs/assets/kidview-brand-board.png`.

Visual principles:

- deep blue primary surfaces
- warm yellow accents
- strong white text contrast
- rounded, reassuring forms
- friendly tone without clutter

Typeface direction:

- `Nunito Rounded`

## Safe Task Ideas For Codex

- convert the iOS scaffold into a complete Xcode project once on macOS
- replace iOS `UserDefaults` PIN handling with Keychain
- implement parent-authenticated exit from child mode
- export and install app icons for Android and iOS
- align Android theme tokens with the new brand palette
- add tests around shared URL parsing behavior

## Tasks That Need Extra Care

- renaming Android package names from `kidtubelock` to `kidview`
- changing signing configuration
- adding networked APIs
- changing child lock behavior on live family devices
- introducing analytics or data collection

## Suggested Prompt Templates

### Android feature work

```text
Inspect the current Android implementation in android/ and implement [feature].
Preserve existing behavior where possible, add tests for new logic, and summarize any risks.
```

### iOS feature work

```text
Inspect the current iOS scaffold in ios/ and implement [feature].
If the task depends on Xcode project wiring, document exactly what must be done on macOS.
```

### Refactor work

```text
Inspect the current implementation and refactor only the scoped area needed for [goal].
Avoid broad renames or unrelated cleanup, and keep behavior stable.
```

## Definition Of Done

A good Codex task result in this repo usually includes:

- code or docs changed in the requested area
- verification notes
- no accidental inclusion of generated files or secrets
- clear summary of what changed and what still needs a Mac, device, or manual follow-up
