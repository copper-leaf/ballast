@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

plugins {
    `copper-leaf-base`
    `copper-leaf-version`
    `copper-leaf-lint`
    `copper-leaf-intellij-plugin`
    id("com.github.gmazzo.buildconfig")
}

description = "Debugger UI application for Ballast MVI"

dependencies {
    // compose Desktop Intellij Plugin
//    compileOnly(compose.desktop.currentOs)
//    implementation(compose.desktop.components.splitPane)
//    implementation(compose.materialIconsExtended)

    // Ktor websocker server
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.serialization)
    implementation(libs.ktor.server.serialization.json)
    implementation(libs.slf4j.nop)

    // Ballast, to manage its own UI state (with debugger artifact to share serialization models between the client and server)
    implementation(project(":ballast-core"))
    implementation(project(":ballast-saved-state"))
    implementation(project(":ballast-debugger"))
}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
        topLevelConstants = true
    }

    buildConfigField("String", "BALLAST_VERSION", "\"${project.version}\"")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    pluginName.set("Ballast")
}
