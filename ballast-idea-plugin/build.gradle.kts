@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

plugins {
    kotlin("jvm")
    `copper-leaf-base`
    `copper-leaf-version`
    `copper-leaf-lint`
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("com.github.gmazzo.buildconfig")
    id("org.jetbrains.intellij")
    id("org.jetbrains.changelog")
}

var projectVersion: ProjectVersion by project.extra
val publishConfiguration: PublishConfiguration = Config.publishConfiguration(project)

description = "Debugger UI application for Ballast MVI"

configurations.all {
    resolutionStrategy.sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
}

dependencies {
    // compose Desktop Intellij Plugin
    compileOnly(compose.desktop.currentOs)
    implementation(compose.desktop.components.splitPane)
    implementation(compose.desktop.components.splitPane)
    implementation(compose.materialIconsExtended)

    // Ktor websocker server
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.websockets)
    implementation(libs.slf4j.nop)
    implementation(libs.ktor.client.cio)

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

    buildConfigField("String", "BALLAST_VERSION", "\"${projectVersion}\"")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    pluginName.set("Ballast")
    type.set("IC")
    version.set("2021.3.2")
    downloadSources.set(true)

    plugins.set(
        listOf(
            "org.jetbrains.kotlin",
            "org.jetbrains.compose.intellij.platform:0.1.0",
        )
    )
}

tasks {
    buildSearchableOptions {
        // temporary workaround
        enabled = false
    }

    signPlugin {
        certificateChain.set(publishConfiguration.jetbrainsMarketplaceCertificateChain)
        privateKey.set(publishConfiguration.jetbrainsMarketplacePrivateKey)
        password.set(publishConfiguration.jetbrainsMarketplacePassphrase)
    }

    publishPlugin {
        token.set(publishConfiguration.jetbrainsMarketplaceToken)
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("221.*")
    }

    runPluginVerifier {
        ideVersions.set(listOf("2020.3.2", "2021.1", "2022.1"))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-Xopt-in=kotlin.ExperimentalStdlibApi"
        freeCompilerArgs += "-Xopt-in=kotlin.time.ExperimentalTime"
        freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        freeCompilerArgs += "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        freeCompilerArgs += "-Xopt-in=androidx.compose.animation.ExperimentalAnimationApi"
        freeCompilerArgs += "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi"
        freeCompilerArgs += "-Xopt-in=androidx.compose.material.ExperimentalMaterialApi"
        freeCompilerArgs += "-Xopt-in=org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi"
    }
}
