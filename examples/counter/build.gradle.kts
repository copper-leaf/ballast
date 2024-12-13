@file:Suppress("UnstableApiUsage")

plugins {
    id("copper-leaf-base")
    id("copper-leaf-android-application")
    id("copper-leaf-targets")
    id("copper-leaf-tests")
    id("copper-leaf-compose")
    id("copper-leaf-lint")
}

kotlin {
    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                optIn("com.copperleaf.ballast.ExperimentalBallastApi")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(project(":ballast-core"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(project(":ballast-test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.swing)
                implementation(project(":ballast-debugger-client"))
                implementation(libs.ktor.client.cio)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.material)
                implementation(libs.androidx.activityCompose)
                implementation(project(":ballast-debugger-client"))
                implementation(libs.ktor.client.cio)
            }
        }

        val iosMain by getting {
            dependencies {
                implementation(project(":ballast-debugger-client"))
                implementation(libs.ktor.client.darwin)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(project(":ballast-debugger-client"))
                implementation(libs.ktor.client.js)
            }
        }

        val wasmJsMain by getting {
            dependencies { }
        }
    }
}

// Compose Desktop config
// ---------------------------------------------------------------------------------------------------------------------

compose {
    desktop {
        application {
            mainClass = "com.copperleaf.ballast.examples.counter.MainKt"
        }
    }
    experimental {
        web {
            application {
            }
        }
    }
}

afterEvaluate {
    tasks.named("wasmJsBrowserTest").configure {
        enabled = false
    }
}
