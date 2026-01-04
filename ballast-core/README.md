# Ballast Core

## Overview

The Ballast Core module provides all the core capabilities of the entire Ballast MVI framework. The Core framework is
robust and opinionated, but also provides many ways to extend the functionality through Interceptors without impacting
the core MVI model. Any additional functionality outside of Core will typically be implemented as an Interceptor and
provided to the `BallastViewModelConfiguration`.

## See Also

- [Ballast API](./../ballast-api/README.md)
- [Ballast Viewmodel](./../ballast-viewmodel/README.md)
- [Ballast Logging](./../ballast-logging/README.md)
- [Ballast Utils](./../ballast-utils/README.md)

## Usage

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
