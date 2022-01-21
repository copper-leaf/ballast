---
---

# Ballast

Opinionated MVI framework for Kotlin Multiplatform

![GitHub release (latest by date)](https://img.shields.io/github/v/release/copper-leaf/ballast)
![Maven Central](https://img.shields.io/maven-central/v/io.github.copper-leaf/ballast-core)
![Kotlin Version](https://img.shields.io/badge/Kotlin-1.5.31-orange)

## Overview

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-core:{{site.version}}")
    implementation("io.github.copper-leaf:ballast-crashlytics:{{site.version}}")
    implementation("io.github.copper-leaf:ballast-firebase-analytics:{{site.version}}")
    testImplementation("io.github.copper-leaf:ballast-test:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-core:{{site.version}}")
                implementation("io.github.copper-leaf:ballast-crashlytics:{{site.version}}")
                implementation("io.github.copper-leaf:ballast-firebase-analytics:{{site.version}}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-test:{{site.version}}")
            }
        }
    }
}
```

## Customization
