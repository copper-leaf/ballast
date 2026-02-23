# Ballast Scheduler Workmanager

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

- [Ballast Scheduler Core](./../ballast-scheduler-core)
- [Ballast Scheduler Cron](./../ballast-scheduler-cron)
- [Ballast Scheduler ViewModel](./../ballast-scheduler-viewmodel)

## Usage


## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-schedules:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-schedules:{{ballastVersion}}")
            }
        }
    }
}
```
