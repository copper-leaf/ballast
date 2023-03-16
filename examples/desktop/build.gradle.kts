@file:Suppress("UnstableApiUsage")

import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    `copper-leaf-base`
    `copper-leaf-version`
    `copper-leaf-lint`
    id("com.github.gmazzo.buildconfig")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

var projectVersion: ProjectVersion by project.extra
description = "Opinionated Application State Management framework for Kotlin Multiplatform"

// Kotlin config
// ---------------------------------------------------------------------------------------------------------------------

kotlin {
    // targets
    jvm { }

    // sourcesets
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

                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.desktop.components.splitPane)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.multiplatformSettings.core)
                implementation(libs.multiplatformSettings.noArg)
            }
        }
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = Config.javaVersion
    targetCompatibility = Config.javaVersion
}
tasks.withType<Test> {
    testLogging {
        showStandardStreams = true
    }
}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
        topLevelConstants = true
    }

    buildConfigField("String", "BALLAST_VERSION", "\"${projectVersion}\"")
}

// Compose Desktop config
// ---------------------------------------------------------------------------------------------------------------------

compose.desktop {
    application {
        mainClass = "com.copperleaf.ballast.examples.MainKt"
    }
}
