# Ballast Queue Exposed Driver

> [!CAUTION]
>
> Experimental. This module may not still have issues or changes in its public API before being considered stable.
> Please use at your own risk, and file Issues for any problems you may encounter.

## Overview

A Driver implementation backed by a database table with Jetbrains Exposed for database access, designed for server-side 
workloads needing high throughput and safe concurrency.

Supports PostgreSQL databases, with experimental support for MySQL and other dialects possibly supported in the future.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ❌         |
| iOS      | ❌         |
| JS       | ❌         |
| WASM JS  | ❌         |

## Supported Database Engines

| Platform   | Supported | Notes                                    |
|------------|-----------|------------------------------------------|
| Postgresql | ✅         |                                          |
| MySQL      | ⚠️        | Exposed migrations not working correctly |
| SQLite     | ❌         | Planned, development not started         |
| MariaDB    | ❌         | Not Planned, but open for contribution   |
| Oracle     | ❌         | Not Planned, but open for contribution   |

## See Also

- [Exposed](https://www.jetbrains.com/exposed/)
- [Ballast Queue Core](./../ballast-queue-core)

## Usage

TODO

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM projects
dependencies {
    implementation("io.github.copper-leaf:ballast-queue-exposed-driver:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-queue-exposed-driver:{{ballastVersion}}")
            }
        }
    }
}
```
