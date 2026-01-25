# Ballast Firebase Crashlytics

## Overview

TODO

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ❌         |
| Android  | ✅         |
| iOS      | ❌         |
| JS       | ❌         |
| WASM JS  | ❌         |

## See Also

- [Ballast Analytics](./../ballast-analytics)
- [Ballast Firebase Analytics](./../ballast-firebase-analytics)
- [Ballast Crash Reporting](./../ballast-crash-reporting)

## Usage

TODO

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-firebase-crashlytics:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-firebase-crashlytics:{{ballastVersion}}")
            }
        }
    }
}
```
