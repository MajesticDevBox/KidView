# Fix Hilt Metadata Version Mismatch

The build is failing because Kotlin 2.2.10 produces metadata version 2.2.0, which Hilt 2.52 (and its bundled `kotlinx-metadata-jvm`) does not yet support. The maximum supported version in the current setup is 2.1.0.

## User Review Required

> [!IMPORTANT]
> I am proposing to upgrade Hilt from `2.52` to `2.60.1`. This is a significant jump and might require minor code adjustments if there were breaking changes, though Hilt is generally stable across these versions.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///G:/Github%20Repos/kidtube/gradle/libs.versions.toml)
- Update `hilt` version from `2.52` to `2.60.1`.

#### [MODIFY] [app/build.gradle.kts](file:///G:/Github%20Repos/kidtube/app/build.gradle.kts)
- If the Hilt upgrade alone doesn't fix it (unlikely but possible with Kotlin 2.2.10), I may need to explicitly add `kapt("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")` to the dependencies.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:kaptDebugKotlin` to verify the Hilt compiler can now process the metadata.
- Run a full build: `./gradlew assembleDebug`.
