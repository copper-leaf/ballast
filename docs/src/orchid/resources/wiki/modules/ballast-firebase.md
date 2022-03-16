---
---

# Firebase

## Crashlytics

## Analytics

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-crashlytics:{{site.version}}")
    implementation("io.github.copper-leaf:ballast-firebase-analytics:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-crashlytics:{{site.version}}")
                implementation("io.github.copper-leaf:ballast-firebase-analytics:{{site.version}}")
            }
        }
    }
}
```
