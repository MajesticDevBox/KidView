# Migrate to AGP 9.0 Built-in Kotlin and compilerOptions DSL

This plan addresses the deprecation warnings and plugin usage warnings related to AGP 9.0 and Kotlin support.

## Proposed Changes

### [gradle.properties](file:///G:/Github%20Repos/KidView/android/gradle.properties)
- Remove `android.builtInKotlin=false`
- Remove `android.newDsl=false`

### [app/build.gradle.kts](file:///G:/Github%20Repos/KidView/android/app/build.gradle.kts)
- Remove `alias(libs.plugins.kotlin.android)` from the `plugins` block.
- Replace the `kotlinOptions` block with `compilerOptions`.
- Use `jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)` (or similar based on actual DSL).

### [build.gradle.kts (root)](file:///G:/Github%20Repos/KidView/android/build.gradle.kts)
- Remove `alias(libs.plugins.kotlin.android) apply false`.

### [libs.versions.toml](file:///G:/Github%20Repos/KidView/android/gradle/libs.versions.toml)
- Remove `kotlin-android` from the `[plugins]` section.

## Verification Plan

### Automated Tests
- Run `gradlew sync` to verify that the warnings are gone and the project syncs successfully.
- Run `gradlew assembleDebug` to ensure the project still builds correctly.
