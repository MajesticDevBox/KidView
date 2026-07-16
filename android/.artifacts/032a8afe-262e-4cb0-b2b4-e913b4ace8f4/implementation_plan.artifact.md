# Implementation Plan: Rename project references to KidView Lock

The goal is to ensure all references to "KidTubeLock" are removed and the app is consistently named "KidView Lock".

## Findings
- The app name in `strings.xml` is already `KidView Lock`.
- The root project name in `settings.gradle.kts` is already `KidView Lock`.
- Most source code references have already been renamed to `KidView`.
- **Mismatch identified**: `AndroidManifest.xml` refers to `.app.KidViewLockApplication`, but the actual class name is `KidViewApplication`.

## Proposed Changes

### [AndroidManifest.xml](file:///G:/Github%20Repos/KidView/android/app/src/main/AndroidManifest.xml)
- [MODIFY] Update `android:name` in the `<application>` tag to `.app.KidViewApplication` to match the existing class.

## Verification Plan
- **Build**: Run `gradlew :app:assembleDebug` to ensure the project still builds and the manifest change is correct.
- **Visual Check**: Verify that "KidTubeLock" no longer appears in any source files (excluding artifacts).
