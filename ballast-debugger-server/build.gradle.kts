plugins {
    `copper-leaf-base`
    `copper-leaf-version`
    `copper-leaf-lint`
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.gmazzo.buildconfig")
}

description = "Debugger UI application for Ballast MVI"

var projectVersion: ProjectVersion by project.extra
val publishConfiguration: PublishConfiguration = Config.publishConfiguration(project)

configurations.all {
    resolutionStrategy.sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
}

dependencies {
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.logging)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.serialization)
    implementation(libs.ktor.server.serialization.json)
    implementation(libs.slf4j.simple)

    testImplementation(libs.junit)


    // Ballast, to manage its own UI state (with debugger artifact to share serialization models between the client and server)
    implementation(project(":ballast-core"))
    implementation(project(":ballast-saved-state"))
    implementation(project(":ballast-debugger"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-opt-in=kotlin.ExperimentalStdlibApi"
        freeCompilerArgs += "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }
}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
        topLevelConstants = true
    }

    buildConfigField("String", "BALLAST_VERSION", "\"${project.version}\"")
}
