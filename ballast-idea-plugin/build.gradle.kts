import com.copperleaf.gradle.projectVersion

plugins {
    id("copper-leaf-base")
    id("copper-leaf-intellij")
    id("copper-leaf-buildConfig")
}

dependencies {
    // Ktor websocket server
    implementation(libs.bundles.ktorEmbeddedServer)
    implementation(libs.kotlinx.datetime)

    api(libs.kotlinx.coroutines.swing)
    api(libs.multiplatformSettings.core)
    api(libs.multiplatformSettings.test)

    // Ballast, to manage its own UI state (with debugger artifact to share serialization models between the client and server)
    api(project(":ballast-core"))
    api(project(":ballast-repository"))
    api(project(":ballast-saved-state"))
    api(project(":ballast-navigation"))
    api(project(":ballast-debugger"))
    api(project(":ballast-debugger-server"))
}

buildConfig {
    projectVersion(project, "BALLAST_VERSION")
}
