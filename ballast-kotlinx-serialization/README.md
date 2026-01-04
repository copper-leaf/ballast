# Ballast Kotlinx Serialization

## Overview

## See Also

- [Ballast Debugger Client](./../ballast-debugger-client/README.md)

## Usage

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-kotlinx-serialization:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-kotlinx-serialization:{{ballastVersion}}")
            }
        }
    }
}
```
