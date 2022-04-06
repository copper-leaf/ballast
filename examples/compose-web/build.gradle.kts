
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    `copper-leaf-base`
    `copper-leaf-version`
    `copper-leaf-lint`
    id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        browser {
            testTask {
                enabled = false
            }
        }
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":ballast-core"))
                implementation(project(":ballast-saved-state"))
                implementation(project(":ballast-debugger"))
                implementation(project(":examples:common"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)
                implementation("io.ktor:ktor-client-js:1.6.7")
            }
        }
    }
}

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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-Xopt-in=kotlin.time.ExperimentalTime"
        freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        freeCompilerArgs += "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        freeCompilerArgs += "-Xopt-in=androidx.compose.animation.ExperimentalAnimationApi"
        freeCompilerArgs += "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi"
        freeCompilerArgs += "-Xopt-in=androidx.compose.material.ExperimentalMaterialApi"
        freeCompilerArgs += "-Xopt-in=org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi"
    }
}
