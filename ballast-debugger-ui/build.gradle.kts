plugins {
    id("copper-leaf-base")
    id("copper-leaf-targets")
    id("copper-leaf-kotest")
    id("copper-leaf-serialization")
    id("copper-leaf-compose")
//    id("copper-leaf-lint")
    id("copper-leaf-publish")
}

kotlin {
    sourceSets {
        all {
            languageSettings.apply {
                optIn("androidx.compose.material.ExperimentalMaterialApi")
                optIn("androidx.compose.foundation.ExperimentalFoundationApi")
                optIn("com.copperleaf.ballast.ExperimentalBallastApi")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(compose.materialIconsExtended)

                // Ktor websocket server
                implementation(libs.kotlinx.datetime)
                implementation(libs.bundles.ktorEmbeddedServer)

                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.multiplatformSettings.core)
                implementation(libs.multiplatformSettings.test)

                // Ballast, to manage its own UI state (with debugger artifact to share serialization models between the client and server)
                implementation(project(":ballast-core"))
                implementation(project(":ballast-repository"))
                implementation(project(":ballast-saved-state"))
                implementation(project(":ballast-navigation"))
                implementation(project(":ballast-debugger-models"))
                implementation(project(":ballast-debugger-server"))
            }
        }
    }
}
