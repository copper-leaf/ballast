# Ballast API

## Overview

These are the fundamental interfaces and internal implementations necessary to create and run a Ballast ViewModel. If 
you're using Ballast ViewModels is an application, you probably should depend on [Ballast Core](./../ballast-core)
to get all the full functionality needed for your application. If you're building a library that uses or extends Ballast's
base functionality, this is the module you should depend on so you don't pull in unnecessary dependencies.

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

`ballast-api` is not intended for direct use in application code. It contains the interfaces and core abstractions that
other Ballast modules and libraries build on. If you are using Ballast in an application, depend on
[Ballast Core](./../ballast-core) instead. If you are building a Ballast extension library or integration, depend on
`ballast-api` to avoid pulling in unnecessary platform-specific dependencies.

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-api:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-api:{{ballastVersion}}")
            }
        }
    }
}
```
