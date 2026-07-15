# Implementation Plan - Fix Android SDK Mismatch

The goal is to resolve the Android SDK version mismatch between the app configuration and its dependencies. Currently, some dependencies require SDK 35-37, while the app's `targetSdk` is still at 34, and the `compileSdk` configuration is not managed centrally in `libs.versions.toml`.

## User Review Required

> [!IMPORTANT]
> I will be updating `compileSdk` and `targetSdk` to **37** to satisfy the requirements of the latest dependencies (like `core-ktx:1.19.0`). Please confirm if you prefer a different SDK version (e.g., 35 or 36).

## Proposed Changes

### Build Configuration Centralization

I will move the SDK versions to `libs.versions.toml` to ensure consistency and centralize management.

#### [MODIFY] [libs.versions.toml](file:///G:/Github Repos/KidView/android/gradle/libs.versions.toml)
- Add `android-compileSdk = "37"` and `android-targetSdk = "37"` to the `[versions]` block.
- Update `coreKtx` version to `1.19.0` to match the used library version and use it as a reference.

#### [MODIFY] [app/build.gradle.kts](file:///G:/Github Repos/KidView/android/app/build.gradle.kts)
- Use `libs.versions.android.compileSdk` for `compileSdk`.
- Use `libs.versions.android.targetSdk` for `targetSdk`.

## Verification Plan

### Automated Tests
- Run `gradle sync` to ensure the project configuration is valid.
- Run `gradle :app:assembleDebug` to verify the build completes successfully with the new SDK settings.

### Manual Verification
- Verify that the IDE no longer reports SDK mismatch warnings in the build files.
