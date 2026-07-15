# KidView

KidView is the clean, dual-platform home for this private family-use project. It carries forward the Android MVP from the local `kidtube` snapshot and reserves a parallel `ios/` workspace for a future native SwiftUI app.

## Current status

- `android/` contains the imported Android MVP source and Gradle wrapper.
- `ios/` contains the SwiftUI source scaffold for the future Xcode project.
- The original `G:\Github Repos\kidtube` folder was left untouched.

## Repository layout

- `android/`: Android Studio project imported from the existing KidTubeLock app
- `ios/`: SwiftUI source scaffold and future Xcode project
- `docs/android-studio-setup.md`: how to open the Android app correctly in Android Studio
- `docs/migration/`: migration notes and what was intentionally excluded

## Import principles

- Source, tests, assets, and Gradle configuration were carried forward.
- Generated build outputs, local machine settings, IDE state, and signing keys were not migrated.
- Git history could not be preserved because the source folder did not contain a `.git` directory.

## Recommended next steps

1. Open `android/` in Android Studio, not the repo root. See `docs/android-studio-setup.md`.
2. Create the Xcode project inside `ios/` when you are ready to begin the iPhone/iPad version.
3. Decide whether to rename the remaining Android-internal `KidTubeLock` identifiers now or later.
