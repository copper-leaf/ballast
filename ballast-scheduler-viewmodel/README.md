# Ballast Scheduler ViewModel

## Overview

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Scheduler Core](./../ballast-scheduler-core/README.md)
- [Ballast Scheduler Cron](./../ballast-scheduler-cron/README.md)

## Usage

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-scheduler-viewmodel:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-scheduler-viewmodel:{{ballastVersion}}")
            }
        }
    }
}
```
