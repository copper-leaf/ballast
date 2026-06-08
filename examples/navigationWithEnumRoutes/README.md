# Navigation Example (Enum Routes)

## Overview

Demonstrates basic navigation and backstack management with Compose and [Ballast Navigation](./../../ballast-navigation).
Routes are defined as an enum class, which is the simpler of the two approaches to defining routes. See also the
[navigationWithCustomRoutes](../navigationWithCustomRoutes) example for a more flexible route definition.

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
./gradlew :examples:navigationWithEnumRoutes:jsBrowserDevelopmentRun
```
Then open http://localhost:8080

**Desktop (JVM):**
```shell
./gradlew :examples:navigationWithEnumRoutes:run
```

**Android:** Open in Android Studio and run on a device or emulator.

## Sources

- [Common](src/commonMain/kotlin/com/copperleaf/ballast/examples/navigation)
- [Android](src/androidMain/kotlin/com/copperleaf/ballast/examples/navigation)
- [iOS](src/iosMain/kotlin/com/copperleaf/ballast/examples/navigation)
- [JS](src/jsMain/kotlin/com/copperleaf/ballast/examples/navigation)
- [JVM](src/jvmMain/kotlin/com/copperleaf/ballast/examples/navigation)
