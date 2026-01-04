# Ballast Firebase Crashlytics

## Overview

## See Also

- [Ballast Analytics](./../ballast-analytics/README.md)
- [Ballast Firebase Analytics](./../ballast-firebase-analytics/README.md)
- [Ballast Crash Reporting](./../ballast-crash-reporting/README.md)

## Usage

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-firebase-crashlytics:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-firebase-crashlytics:{{ballastVersion}}")
            }
        }
    }
}
```
