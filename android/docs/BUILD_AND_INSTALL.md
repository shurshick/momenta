# Build & Install

## Requirements

- Android Studio Hedgehog (2023.1.1) или новее
- JDK 17
- Android SDK 34

## Build

```bash
# Clean build dev debug
./gradlew clean assembleDevDebug

# Install on device/emulator
./gradlew installDevDebug
```

## Build Variants

| Variant | Command |
|---------|---------|
| Dev Debug | `./gradlew assembleDevDebug` |
| Staging Debug | `./gradlew assembleStagingDebug` |
| Prod Release | `./gradlew assembleProdRelease` |

## APK Location

`app/build/outputs/apk/<flavor>/debug/`
