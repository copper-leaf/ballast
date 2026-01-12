# Ballast Autoscale

## Overview

`AutoscalingViewModel` acts as a wrapper around a pool of other ViewModels, and provides basic facilities for scaling 
the pool of ViewModels up or down to adapt to load, and distributing work among the pool of ViewModel workers. The main
use-case would be in server-side applications such as job queue processors. For example, one could increase the 
parallelism of processing jobs in the queue in response to the number of pending jobs, average time spent waiting for a 
job to start, etc.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Ktor Server](./../ballast-ktor-server/README.md)

## Usage

TODO

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-autoscale:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-autoscale:{{ballastVersion}}")
            }
        }
    }
}
```
