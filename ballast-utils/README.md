# Ballast Utils

## Overview

Helper functions and a configuration DSL used throughout the Ballast framework. This module is included transitively
via [Ballast Core](./../ballast-core) and you generally do not need to depend on it directly.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Core](./../ballast-core)

## Usage

`ballast-utils` is not intended for direct use in application code. It is pulled in transitively when you depend on
[Ballast Core](./../ballast-core). The utilities and DSL helpers it provides are used internally by other Ballast
modules.

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-utils:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-utils:{{ballastVersion}}")
            }
        }
    }
}
```

