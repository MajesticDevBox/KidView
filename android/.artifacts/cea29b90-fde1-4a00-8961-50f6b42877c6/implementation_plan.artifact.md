# Implementation Plan - Fix Hilt Build Error

The build is failing with the error: `Could not find class file for 'com.example.kidtubelock.app.KidTubeLockApplication'`. This is a common issue after a project rename or move, where stale build artifacts, Gradle caches, or incremental compilation metadata still refer to old class names and absolute paths.

## Research Findings

- **Rename Completed**: The project source code has been renamed to use the `com.mdev.kidview` package. The application class is now `KidViewApplication`.
- **Manifest Updated**: `AndroidManifest.xml` correctly points to `.app.KidViewApplication`.
- **Stale Caches**: Global searches revealed that the `.gradle` folder and a `.kotlin` log file still contain references to the old `com.example.kidtubelock` package and absolute paths to a previous project location (`G:\Github Repos\kidtube`).
- **Build Success**: A clean build (`./gradlew clean :app:hiltJavaCompileDebug`) succeeded in the current environment, confirming the source code itself is correct.

## User Review Required

> [!IMPORTANT]
> This fix requires deleting several hidden cache directories (`.gradle`, `.kotlin`) and build directories. While safe, it will trigger a full rebuild and a complete re-download of Gradle dependencies if the global Gradle cache is also affected (though we will focus on the project-local cache first).

## Proposed Changes

### Build and Cache Cleanup

I will perform a deep clean of the project to remove any lingering metadata from the old project name.

#### [DELETE] `.gradle/` directory
Remove the project-local Gradle cache which contains absolute paths and execution history referring to the old `kidtube` location.

#### [DELETE] `.kotlin/` directory
Remove Kotlin incremental compilation metadata that might be confused by the rename.

#### [DELETE] `app/build/` and root `build/` directories
Ensure all generated Hilt "Aggregated Root" files are removed.

## Verification Plan

### Automated Verification
- Run `./gradlew clean` to ensure a baseline state.
- Run `./gradlew :app:hiltJavaCompileDebug` to verify that Hilt can now correctly process the `KidViewApplication` root without looking for the old class.
- Run a full build: `./gradlew :app:assembleDebug`.

### Manual Verification
- Verify that the error message no longer appears in the Gradle console.
