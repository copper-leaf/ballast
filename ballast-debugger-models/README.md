# Ballast Debugger Models

## Overview

Shared data models used by both [Ballast Debugger Client](./../ballast-debugger-client) and the Ballast IntelliJ Plugin
for inspecting the internal state and activity of Ballast ViewModels. Typically you do not need to depend on this module
directly; use [Ballast Debugger Client](./../ballast-debugger-client) instead.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Debugger Client](./../ballast-debugger-client)

## Usage

`ballast-debugger-models` is not intended for direct use in application code. Use
[Ballast Debugger Client](./../ballast-debugger-client) to connect your ViewModels to the Ballast IntelliJ Plugin.

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-debugger-models:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-debugger-models:{{ballastVersion}}")
            }
        }
    }
}
```
