# KidTube Import - 2026-07-15

## Source inspected

- Local source snapshot: `G:\Github Repos\kidtube`
- GitHub repo inventory inspected through the GitHub connector for accounts `majestic44` and `Mc-Handyman-LLC`

## Outcome

Created a new additive repository workspace at `G:\Github Repos\KidView` for future Android and iOS development.

## Files and folders migrated

- `android/app/src/`
- `android/app/build.gradle.kts`
- `android/app/proguard-rules.pro`
- `android/gradle/`
- `android/gradlew`
- `android/gradlew.bat`
- `android/build.gradle.kts`
- `android/settings.gradle.kts`
- `android/gradle.properties`
- `android/README.md` based on the original project README

## Files and folders intentionally not migrated

- `.artifacts/`
- `.gradle/`
- `.idea/`
- `.kotlin/`
- `.vscode/`
- `build/`
- `app/build/`
- `local.properties`
- `keys/` including `kidtubelock.jks`

## History note

Git history could not be preserved because `G:\Github Repos\kidtube` did not contain a `.git` directory or remote metadata. The new repository starts from a clean import commit instead.

## Follow-up note

The Android app still uses `KidTubeLock` as its Gradle root project name and package namespace internally. That was left in place to avoid accidental behavioral changes during the repository split.
