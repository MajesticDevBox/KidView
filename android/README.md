# KidView

KidView is a private family-use Android app MVP for saving approved YouTube links and launching a locked-down child-mode placeholder screen. This first pass focuses on a clean, buildable foundation using Kotlin, Jetpack Compose, Material 3, Navigation Compose, Hilt, ViewModel, and DataStore.

## Current MVP scope

- Requires first-run parent PIN creation before showing the main app
- Stores the parent PIN locally as a salted SHA-256 hash
- Lets a parent save approved YouTube video or playlist links
- Parses common YouTube URLs and stores only the extracted media ID plus local metadata
- Starts a child-mode placeholder screen with immersive mode and keep-screen-on behavior
- Includes a lock seam for a future dedicated-device mode based on Android Lock Task Mode

## Package layout

- `app`: application entry point, dependency injection, navigation, and theme
- `domain`: models and pure utility logic such as PIN hashing and YouTube URL parsing
- `data`: DataStore-backed persistence and repository implementation
- `feature_parent`: PIN setup, home, and add-media flows
- `feature_player`: child-mode placeholder screen
- `feature_lock`: lock abstraction seam for future behavior

## Build notes

1. Open the project in Android Studio.
2. Make sure the Android SDK for API 35 is installed.
3. Let Android Studio create `local.properties` if it is missing.
4. Sync Gradle and run the `app` configuration.

## What is intentionally not implemented yet

- Real YouTube playback
- A parent unlock flow from child mode
- Strong dedicated-device enforcement with Android Lock Task Mode
- Backend services, sign-in, analytics, search, or ads
