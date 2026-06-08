# Counter Example

## Overview

A minimal counter demonstrating the bare basics of sending Inputs and updating ViewModel State with Ballast. This is
the best starting point for understanding how the core MVI loop works.

## Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## Running Locally

**Browser (JS):**
```shell
./gradlew :examples:counter:jsBrowserDevelopmentRun
```
Then open http://localhost:8080

**Desktop (JVM):**
```shell
./gradlew :examples:counter:run
```

**Android:** Open in Android Studio and run on a device or emulator.

## Sources

- [Common](src/commonMain/kotlin/com/copperleaf/ballast/examples/counter)
- [Android](src/androidMain/kotlin/com/copperleaf/ballast/examples/counter)
- [iOS](src/iosMain/kotlin/com/copperleaf/ballast/examples/counter)
- [JS](src/jsMain/kotlin/com/copperleaf/ballast/examples/counter)
- [JVM](src/jvmMain/kotlin/com/copperleaf/ballast/examples/counter)
