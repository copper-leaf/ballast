@file:Suppress("UnstableApiUsage")

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.compose.compose

plugins {
    id("com.android.application")
    kotlin("multiplatform")
    `copper-leaf-base`
    `copper-leaf-version`
    `copper-leaf-lint`
    id("com.github.gmazzo.buildconfig")
    id("org.jetbrains.compose")
}

var projectVersion: ProjectVersion by project.extra
description = "Opinionated Application State Management framework for Kotlin Multiplatform"

// Android config
// ---------------------------------------------------------------------------------------------------------------------

android {
    compileSdk = 31
    defaultConfig {
        minSdk = 21
        targetSdk = 31
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        val release by getting {
            isMinifyEnabled = false
        }
    }
    sourceSets {
        getByName("main") {
            setRoot("src/androidMain")
        }
        getByName("androidTest") {
            setRoot("src/androidTest")
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
    lint {
        disable += listOf("GradleDependency")
    }
    buildFeatures {
        viewBinding = true
    }
}

// Kotlin config
// ---------------------------------------------------------------------------------------------------------------------

kotlin {
    // targets
    jvm { }
    android { }
    js(IR) {
        browser {
            testTask {
                enabled = false
            }
        }
        binaries.executable()
    }
//    ios { }

    // sourcesets
    sourceSets {
        all {
            languageSettings.apply {
                optIn("androidx.compose.material.ExperimentalMaterialApi")
                optIn("com.copperleaf.ballast.ExperimentalBallastApi")
            }
        }

        // Common Sourcesets
        val commonMain by getting {
            dependencies {
                implementation(project(":ballast-core"))
                implementation(project(":ballast-repository"))
                implementation(project(":ballast-saved-state"))
                implementation(project(":ballast-debugger"))
                implementation(project(":ballast-sync"))

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.multiplatformSettings.core)
                implementation(libs.multiplatformSettings.noArg)
            }
        }

        val composeMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
            }
        }

        val androidMain by getting {
            dependsOn(composeMain)
            dependencies {
                implementation(libs.androidx.core)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.material)
                implementation(libs.androidx.lifecycle.runtime)
                implementation(libs.androidx.compose.activity)
                implementation(libs.androidx.navigation)
                implementation(libs.androidx.navigation.ui)
                implementation(libs.androidx.navigation.compose)

                implementation(libs.ktor.client.okhttp)
            }
        }

        val jvmMain by getting {
            dependsOn(composeMain)
            dependencies {
                api(compose.desktop.currentOs)

                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.desktop.components.splitPane)

                implementation(libs.ktor.client.okhttp)
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)

                implementation(libs.ktor.client.js)
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

// Cache APIs because of stupid CORS...
// ---------------------------------------------------------------------------------------------------------------------

val fetchLatestBggApis by tasks.registering {
    outputs.upToDateWhen { false }
    doLast {
        listOf(
            "boardgame",
            "rpg",
            "videogame",
            "boardgameperson",
            "rpgperson",
            "boardgamecompany",
            "rpgcompany",
            "videogamecompany",
        ).forEach {
            executeAndGetXmlResponse(it)
        }
    }
}
tasks.getByName("jsProcessResources").dependsOn(fetchLatestBggApis)

fun executeAndGetXmlResponse(type: String) {
    val url = "https://boardgamegeek.com/xmlapi2/hot?type=$type"
    val request = Request.Builder()
        .url(url)
        .get()
        .build()

    val client = OkHttpClient()

    println("GET --> $url")
    val response = client.newCall(request).execute()
    println("${response.code} ${response.message} <-- $url")

    val parentDir = project.projectDir.resolve("src/jsMain/resources/bgg/hot")
    parentDir.mkdirs()

    val responseFile = parentDir.resolve("$type.xml")

    responseFile.outputStream().write(
        response.body!!.bytes()
    )
}

// Compose Desktop config
// ---------------------------------------------------------------------------------------------------------------------

compose.desktop {
    application {
        mainClass = "com.copperleaf.ballast.examples.MainKt"
    }
}

//afterEvaluate {
//    // Remove log pollution until Android support in KMP improves.
//    project.extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()?.let { kmpExt ->
//        kmpExt.sourceSets.removeAll {
//            setOf(
//                "androidAndroidTestRelease",
//                "androidTestFixtures",
//                "androidTestFixturesDebug",
//                "androidTestFixturesRelease",
//            ).contains(it.name)
//        }
//    }
//}
