# Android Studio Setup

## Important

Do not open the repository root `G:\Github Repos\KidView` as the Android app project.

The runnable Android Gradle project lives in:

- `G:\Github Repos\KidView\android`

If you open the repo root directly, Android Studio may not detect any Android modules because the Gradle Android project is nested under `android/`.

## Correct folder to open

Open:

- `G:\Github Repos\KidView\android`

Not:

- `G:\Github Repos\KidView`

## Steps

1. Launch Android Studio.
2. Choose `Open`.
3. Select the folder `G:\Github Repos\KidView\android`.
4. Let Gradle sync finish.
5. Confirm the `app` module appears in the project view.
6. Run the `app` configuration.

## Files Android Studio should detect

- `android/settings.gradle.kts`
- `android/build.gradle.kts`
- `android/app/build.gradle.kts`

These define the Android project and the `app` module.

## If no modules appear

Try:

1. Close the project.
2. Re-open `G:\Github Repos\KidView\android`.
3. If prompted, trust the Gradle project and allow sync.
4. In the Gradle tool window, refresh the project.

## Notes

- The repository root still contains shared assets, docs, and iOS work.
- The Android app itself is intentionally isolated under `android/`.
- This layout is normal for a dual-platform repository where Android and iOS live side by side.
