# Ballast Logging

## Overview

## See Also

- [Ballast Core](./../ballast-core/README.md)

## Usage

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-logging:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-logging:{{ballastVersion}}")
            }
        }
    }
}
```
