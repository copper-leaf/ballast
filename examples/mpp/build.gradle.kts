
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
        disable("GradleDependency")
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
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("androidx.compose.material.ExperimentalMaterialApi")
            }
        }

        // Common Sourcesets
        val commonMain by getting {
            dependencies {
                implementation(project(":ballast-core"))
                implementation(project(":ballast-repository"))
                implementation(project(":ballast-saved-state"))
                implementation(project(":ballast-debugger"))

                implementation("io.ktor:ktor-client-core:1.6.7")
                implementation("io.ktor:ktor-client-logging:1.6.7")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
                implementation("com.russhwolf:multiplatform-settings:0.8.1")
                implementation("com.russhwolf:multiplatform-settings-no-arg:0.8.1")
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
                implementation("androidx.core:core-ktx:1.7.0")
                implementation("androidx.appcompat:appcompat:1.4.1")
                implementation("com.google.android.material:material:1.5.0")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
                implementation("androidx.activity:activity-compose:1.4.0")
                implementation("androidx.navigation:navigation-fragment-ktx:2.4.1")
                implementation("androidx.navigation:navigation-ui-ktx:2.4.1")
                implementation("androidx.navigation:navigation-compose:2.4.1")

                implementation("io.ktor:ktor-client-okhttp:1.6.7")
            }
        }

        val jvmMain by getting {
            dependsOn(composeMain)
            dependencies {
                api(compose.desktop.currentOs)

                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.desktop.components.splitPane)

                implementation("io.ktor:ktor-client-okhttp:1.6.7")
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)

                implementation("io.ktor:ktor-client-js:1.6.7")
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
