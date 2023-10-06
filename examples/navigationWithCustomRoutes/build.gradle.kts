@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension


plugins {
    id("copper-leaf-base")
    id("copper-leaf-android-application")
    id("copper-leaf-targets")
    id("copper-leaf-kotest")
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
                implementation(project(":ballast-navigation"))
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
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.material)
                implementation(libs.androidx.activityCompose)
            }
        }

        val jsMain by getting {
            dependencies {
            }
        }
    }
}

// Compose Desktop config
// ---------------------------------------------------------------------------------------------------------------------

compose {
    desktop {
        application {
            mainClass = "com.copperleaf.ballast.examples.navigation.MainKt"
        }
    }
    experimental {
        web {
            application {
            }
        }
    }
}

rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport = YarnLockMismatchReport.NONE
    rootProject.the<YarnRootExtension>().reportNewYarnLock = false
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = true
}

tasks.named("jsBrowserProductionWebpack").configure {
    enabled = false
}
