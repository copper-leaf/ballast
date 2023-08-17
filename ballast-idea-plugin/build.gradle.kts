import com.copperleaf.gradle.projectVersion

plugins {
    id("copper-leaf-base")
    id("copper-leaf-intellij")
    id("copper-leaf-buildConfig")
}

dependencies {
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

buildConfig {
    projectVersion(project, "BALLAST_VERSION")
}
