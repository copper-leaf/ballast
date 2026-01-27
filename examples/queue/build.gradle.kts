@file:Suppress("UnstableApiUsage")

plugins {
    id("copper-leaf-base")
    id("copper-leaf-targets")
    id("copper-leaf-tests")
    id("copper-leaf-compose")
    id("copper-leaf-serialization")
    id("copper-leaf-lint")
}

kotlin {
    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.uuid.ExperimentalUuidApi")
                optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                optIn("org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi")
            }
        }

        val jvmMain by getting {
            dependencies {
                api("org.postgresql:postgresql:42.7.7")
                api("com.mysql:mysql-connector-j:9.5.0")

                api("org.jetbrains.exposed:exposed-r2dbc:1.0.0")
                implementation("org.postgresql:r2dbc-postgresql:1.1.1.RELEASE")

                implementation(project(":ballast-core"))
                implementation(project(":ballast-queue-core"))
                implementation(project(":ballast-queue-exposed-driver"))
                implementation(project(":ballast-queue-viewmodel"))
                implementation(project(":ballast-kotlinx-serialization"))
                implementation(project(":ballast-autoscale"))

                implementation(compose.materialIconsExtended)
                implementation(compose.material3)
                implementation(libs.kotlinx.coroutines.swing)

                implementation("io.github.oleksandrbalan:lazytable:1.10.0")
            }
        }
    }
}

// Compose Desktop config
// ---------------------------------------------------------------------------------------------------------------------

compose.desktop {
    application {
        mainClass = "com.copperleaf.ballast.examples.MainKt"
    }
}
