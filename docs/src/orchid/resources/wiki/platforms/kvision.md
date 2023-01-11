---
---

# {{ page.title }}

[KVision][1] is an object-oriented web framework for Kotlin/JS, providing a more traditional MVC approach to building
Kotlin web applications as compared to Compose. KVision has modules to support binding Ballast ViewModels to KVision
UIs.

[![rjaros/kvision - GitHub](https://gh-card.dev/repos/rjaros/kvision.svg?fullname=)][2]

## KVision-Ballast

The [kvision-ballast][3] module adds extension functions which bind a Ballast ViewModel's State to the KVision UI. 
Whenever the ViewModel emits a new State, the corresponding portion of the KVision UI will be updated with the new data.

```kotlin
class KVisionBallastExample : Application(), KoinComponent {

    private val exampleViewModel: ExampleViewModel by inject()
    
    override fun start() {
        root("ballast-example") {
            section().bind(todoViewModel) { state ->
                // ...
            }
        }
    }
}
```

## KVision-Routing-Ballast

The [kvision-ballast][3] module wraps the [Ballast Navigation][5] routing library in KVision's `KVRouter`, allowing 
Ballast's navigation to be integrated more cleanly into a KVision application.

## Installation

KVision and its Ballast integrations are both maintained by the KVision community, separately from Ballast. Refer to the
official [KVision documentation][1] for getting started with KVision, and the [KVision TODOMVC][6] example project for
using Ballast in KVision.

```kotlin
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("js") version kotlinVersion
    val kvisionVersion: String by System.getProperties()
    id("io.kvision") version kvisionVersion
}

version = "1.0.0-SNAPSHOT"
group = "com.example"

repositories {
    mavenCentral()
    jcenter()
    mavenLocal()
}

// Versions
val kotlinVersion: String by System.getProperties()
val kvisionVersion: String by System.getProperties()

val webDir = file("src/main/web")

kotlin {
    js {
        browser {
            runTask {
                outputFileName = "main.bundle.js"
                sourceMaps = false
                devServer = KotlinWebpackConfig.DevServer(
                    open = false,
                    port = 3000,
                    proxy = mutableMapOf(
                        "/kv/*" to "http://localhost:8080",
                        "/kvws/*" to mapOf("target" to "ws://localhost:8080", "ws" to true)
                    ),
                    static = mutableListOf("$buildDir/processedResources/js/main")
                )
            }
            webpackTask {
                outputFileName = "main.bundle.js"
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }
    sourceSets["main"].dependencies {
        implementation("io.kvision:kvision:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap:$kvisionVersion")
        implementation("io.kvision:kvision-i18n:$kvisionVersion")
        implementation("io.kvision:kvision-ballast:$kvisionVersion")
        implementation("io.kvision:kvision-routing-ballast:$kvisionVersion")
    }
    sourceSets["test"].dependencies {
        implementation(kotlin("test-js"))
        implementation("io.kvision:kvision-testutils:$kvisionVersion")
    }
    sourceSets["main"].resources.srcDir(webDir)
}
```

[1]: https://kvision.io/
[2]: https://github.com/rjaros/kvision-examples/tree/master/todomvc-ballast
[3]: https://github.com/rjaros/kvision/tree/master/kvision-modules/kvision-ballast
[4]: https://github.com/rjaros/kvision/tree/master/kvision-modules/kvision-routing-ballast
[5]: {{ 'Ballast Navigation' | link }}
[6]: {{ 'KVision TODOMVC' | link }}
