# Ballast Debugger Client

## Overview

## See Also

- [Ballast Kotlinx Serialization](./../ballast-kotlinx-serialization/README.md)

## Usage

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-debugger-client:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-debugger-client:{{ballastVersion}}")
            }
        }
    }
}
```
