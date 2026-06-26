# Build & Install

## Requirements

- Android Studio Hedgehog (2023.1.1) или новее
- JDK 17
- Android SDK 34

## Build

```bash
# Clean build prod debug (основной вариант для установки)
./gradlew clean assembleProdDebug

# Install on device/emulator
./gradlew installProdDebug
```

## Build Variants

| Variant | Command |
|---------|---------|
| Prod Debug | `./gradlew assembleProdDebug` |
| Staging Debug | `./gradlew assembleStagingDebug` |
| Dev Debug | `./gradlew assembleDevDebug` |
| Prod Release | `./gradlew assembleProdRelease` |

## APK Location

`app/build/outputs/apk/<flavor>/debug/`
