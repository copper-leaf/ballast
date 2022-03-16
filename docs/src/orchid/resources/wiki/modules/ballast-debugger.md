---
---

# Ballast Debugger

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-debugger:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-debugger:{{site.version}}")
            }
        }
    }
}
```

<div id="intellij-plugin-card"></div>
<div id="intellij-plugin-button"></div>

<script src="https://plugins.jetbrains.com/assets/scripts/mp-widget.js"></script>
<script>
  // Please, replace #yourelement with a real element id on your webpage
  MarketplaceWidget.setupMarketplaceWidget('card', 18702, "#intellij-plugin-card");
</script>
<script>
  // Please, replace #yourelement with a real element id on your webpage
  MarketplaceWidget.setupMarketplaceWidget('install', 18702, "#intellij-plugin-button");
</script>
