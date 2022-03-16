---
---

# Test

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    testImplementation("io.github.copper-leaf:ballast-test:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-test:{{site.version}}")
            }
        }
    }
}
```
