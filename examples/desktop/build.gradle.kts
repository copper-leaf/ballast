@file:Suppress("UnstableApiUsage")

import com.copperleaf.gradle.projectVersion

plugins {
    id("copper-leaf-base")
    id("copper-leaf-targets")
    id("copper-leaf-kotest")
    id("copper-leaf-buildConfig")
    id("copper-leaf-compose")
    id("copper-leaf-serialization")
//    id("copper-leaf-lint")
}

kotlin {
    sourceSets {
        all {
            languageSettings.apply {
                optIn("androidx.compose.material.ExperimentalMaterialApi")
                optIn("com.copperleaf.ballast.ExperimentalBallastApi")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(":ballast-core"))
                implementation(project(":ballast-repository"))
                implementation(project(":ballast-saved-state"))
                implementation(project(":ballast-debugger"))
                implementation(project(":ballast-sync"))
                implementation(project(":ballast-undo"))
                implementation(project(":ballast-navigation"))

                implementation(libs.bundles.ktorClient)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.multiplatformSettings.core)
                implementation(libs.multiplatformSettings.noArg)
            }
        }
    }
}

buildConfig {
    projectVersion(project, "BALLAST_VERSION")
}

// Compose Desktop config
// ---------------------------------------------------------------------------------------------------------------------

compose.desktop {
    application {
        mainClass = "com.copperleaf.ballast.examples.MainKt"
    }
}
