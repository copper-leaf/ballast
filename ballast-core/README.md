# Ballast Core

## Overview

The Ballast Core module provides all the core capabilities of the entire Ballast MVI framework. This module is simply an
aggregation of other fundamental Ballast modules, which are combined to provide the basic functionality and 
platform-specific integrations needed for developing application, and is the primary module you should include when
using Ballast for building applications. Library developers building additional features or integrations into Ballast 
should depend on [Ballast API](./../ballast-api) instead, since a library should not need the 
platform-specific features provided by the other modules.

Refer to the [Getting Started guide](./) for basic setup and using of the Ballast MVI framework as a whole. Refer to 
documentation for each module linked in the [See Also](#see-also) section of this page for configuration of the 
platform-specific integrations.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast API](./../ballast-api)
- [Ballast Viewmodel](./../ballast-viewmodel)
- [Ballast Logging](./../ballast-logging)
- [Ballast Utils](./../ballast-utils)

## Usage

TODO

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-core:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-core:{{ballastVersion}}")
            }
        }
    }
}
```
