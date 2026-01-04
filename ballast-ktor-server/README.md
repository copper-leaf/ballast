# Ballast Ktor Server

## Overview

## See Also

- [Ballast Autoscale](./../ballast-autoscale/README.md)

## Usage

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM projects
dependencies {
    implementation("io.github.copper-leaf:ballast-ktor-server:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-ktor-server:{{ballastVersion}}")
            }
        }
    }
}
```
