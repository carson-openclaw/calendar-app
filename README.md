# Aura Calendar

A calm, elegant calendar app built with Jetpack Compose and Material 3.

## Opening in Android Studio

1. Open **Android Studio** (Ladybug or newer recommended)
2. Select **File > Open** and navigate to this project root
3. Wait for Gradle sync to complete
4. Let Android Studio download the required SDK components

## Building an APK

```bash
# Debug APK
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk

# Release APK (unsigned — requires signing config for production)
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

## Requirements

- Android Studio Ladybug (2024.2) or newer
- JDK 17
- Android SDK 34
- Kotlin 2.0.20+

## Architecture

- **UI**: Jetpack Compose with Material 3
- **Theme**: Custom `AuraTheme` — warm paper backgrounds, muted ink typography, soft sage accent
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
