@file:Suppress("UnstableApiUsage")

import com.copperleaf.gradle.projectVersion
import okhttp3.OkHttpClient
import okhttp3.Request

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
    // sourcesets
    sourceSets {
        all {
            languageSettings.apply {
                optIn("com.copperleaf.ballast.ExperimentalBallastApi")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(project(":ballast-core"))
                implementation(project(":ballast-repository"))
                implementation(project(":ballast-saved-state"))
                implementation(project(":ballast-debugger-client"))
                implementation(project(":ballast-sync"))
                implementation(project(":ballast-undo"))
                implementation(project(":ballast-navigation"))

                implementation(compose.html.core)
                implementation(libs.bundles.ktorClient)
                implementation(libs.ktor.client.js)
                implementation(libs.multiplatformSettings.core)
                implementation(libs.multiplatformSettings.noArg)
            }
        }
    }
}

buildConfig {
    projectVersion(project, "BALLAST_VERSION")
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
//tasks.getByName("jsProcessResources").dependsOn(fetchLatestBggApis)

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
