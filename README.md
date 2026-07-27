This repository is for developers who want to build publicly distributed applications integrating Aidlab and Aidmed One. The Android example demonstrates how to connect and pair with devices, helping you save time and accelerate development.

For answers to common questions about Aidlab, visit our [developer portal](https://www.aidlab.com/developer).

## Technical overview

- Requires Android 9+ (API level 28+)
- compileSdk 36, targetSdk 36
- Uses Aidlab Android SDK 2.5.1 from Maven Central by default
- Works with recent Android Studio versions (e.g., 2025.1.2+)

## Build

Open this repository directly in Android Studio or run:

```bash
./gradlew build
```

When developing the SDK from the Aidlab SDK monorepo, explicitly use the
adjacent SDK sources:

```bash
./gradlew -PuseLocalAidlabSdk=true build
```
