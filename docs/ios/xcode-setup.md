# Xcode Setup For KidView

This document walks through creating the iOS Xcode project on the Mac and attaching the scaffold files already stored in this repository.

## What has already been prepared

The repository now includes source-only iOS scaffolding in:

- `ios/KidView/`
- `ios/KidViewTests/`

These files are meant to be added into a new native SwiftUI app target on macOS.

## Recommended project settings

- Product Name: `KidView`
- Interface: `SwiftUI`
- Language: `Swift`
- Testing: include unit tests
- Bundle Identifier suggestion: `com.mdev.kidview`
- Minimum iOS version: `iOS 17.0` or newer

## Create the Xcode project

1. On the Mac, open Xcode.
2. Choose `Create a new Xcode project`.
3. Select `iOS` then `App`.
4. Set:
   - Product Name: `KidView`
   - Team: your Apple ID team
   - Organization Identifier: `com.mdev`
   - Interface: `SwiftUI`
   - Language: `Swift`
   - Include Tests: enabled
5. Save the project inside `G:\Github Repos\KidView\ios` on the shared repo location, or in the equivalent path on the Mac if that drive is mounted differently.

Expected result:

- `ios/KidView.xcodeproj`
- `ios/KidView/`
- `ios/KidViewTests/`

## Replace the generated source with the repo scaffold

After Xcode creates the project:

1. Keep the generated `.xcodeproj`.
2. Delete the generated Swift placeholder files from the Xcode navigator, but do not delete the project file itself.
3. Drag these folders from Finder into the Xcode navigator:
   - `ios/KidView/App`
   - `ios/KidView/Domain`
   - `ios/KidView/Features`
   - `ios/KidView/Player`
   - `ios/KidView/Security`
   - `ios/KidView/Storage`
   - `ios/KidView/Support`
   - `ios/KidViewTests`
4. When prompted:
   - choose `Copy items if needed` only if your Mac project is not already using the repo files directly
   - choose `Create groups`
   - add the app files to the `KidView` target
   - add the test file to the `KidViewTests` target

## Important target checks

Open the `KidView` target and confirm:

- `Signing & Capabilities`
  - your team is selected
  - a valid bundle identifier is set
- `General`
  - deployment target is the version you want
  - portrait orientation is enabled at minimum
- `Build Settings`
  - Swift version stays on the Xcode default

## First build expectations

The scaffold is designed as a starting point, not a finished shipping app. On the first Xcode build, you should expect to verify:

- the app launches into parent PIN setup
- approved YouTube links can be added
- selecting a media item presents a child-mode screen
- the player loads through `WKWebView`
- the Guided Access status banner updates when Guided Access changes

## Notes about the current scaffold

- Parent PIN storage currently uses `UserDefaults` plus a SHA-256 hash for starter development speed.
- For a more production-ready private family build, move the PIN material into Keychain.
- The child-mode screen currently exposes an `End` button for development convenience.
- The current player wrapper is intentionally simple and uses a direct embedded YouTube frame.
- The internal naming still mirrors the Android MVP architecture so both platforms stay aligned.

## Recommended next iOS tasks

1. Replace the temporary `End` button with a parent unlock flow.
2. Move parent PIN storage from `UserDefaults` to Keychain.
3. Add app icons and launch assets in `Assets.xcassets`.
4. Add a dedicated Guided Access instruction screen before child mode.
5. Decide whether the iOS app name should remain `KidView` while Android internals still reference `KidTubeLock`.

## Git workflow note

The iOS scaffold files are already committed in the repository. Once the `.xcodeproj` is created on the Mac, commit that project file separately so the repo cleanly records the transition from source scaffold to a runnable Xcode app.
