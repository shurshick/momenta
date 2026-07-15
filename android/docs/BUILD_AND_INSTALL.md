# Build & Install

## Requirements

- Android Studio Hedgehog (2023.1.1) или новее
- JDK 17
- Android SDK 34

## Build

```bash
# Clean build prod debug (основной вариант для установки)
./gradlew clean assembleProdRelease

# Verify APK installability
./gradlew verifyInstallableProdReleaseApk

# Install on device/emulator
./gradlew installProdDebug
```

## Build Variants

| Variant | Command |
|---------|---------|
| Prod Debug | `./gradlew assembleProdDebug` |
| Prod Release installability check | `./gradlew verifyInstallableProdReleaseApk` |
| Staging Debug | `./gradlew assembleStagingDebug` |
| Dev Debug | `./gradlew assembleDevDebug` |
| Prod Release | `./gradlew assembleProdRelease` |

## APK Location

`app/build/outputs/apk/<flavor>/debug/`

Main installable APK:

`app/build/outputs/apk/prod/release/app-prod-release.apk`
