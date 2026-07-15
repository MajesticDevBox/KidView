# Walkthrough - Hilt Metadata Fix

I have resolved the Hilt metadata version mismatch by upgrading the Hilt library to a version compatible with Kotlin 2.2.10.

## Changes

### Build Configuration

#### [libs.versions.toml](file:///G:/Github%20Repos/kidtube/gradle/libs.versions.toml)

Updated the Hilt version to `2.60.1`.

```diff
 [versions]
 agp = "9.3.0"
 kotlin = "2.2.10"
 ...
-hilt = "2.52"
+hilt = "2.60.1"
```

## Verification Results

### Automated Tests
- Ran `./gradlew :app:kaptDebugKotlin`: **SUCCESS**
- Ran `./gradlew assembleDebug`: **SUCCESS**

The build no longer fails with `java.lang.IllegalArgumentException: Provided Metadata instance has version 2.2.0, while maximum supported version is 2.1.0`.
