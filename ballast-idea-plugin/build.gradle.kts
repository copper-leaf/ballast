@file:Suppress("OPT_IN_IS_NOT_ENABLED")
@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

plugins {
    `copper-leaf-base`
    `copper-leaf-version`
    `copper-leaf-lint`

    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.intellij") version "1.10.0"
    id("org.jetbrains.changelog") version "1.3.1"

    id("com.github.gmazzo.buildconfig")
}

description = "Debugger UI application for Ballast MVI"

var projectVersion: ProjectVersion by project.extra
val publishConfiguration: PublishConfiguration = Config.publishConfiguration(project)

configurations.all {
    resolutionStrategy.sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
}

dependencies {
    // compose Desktop Intellij Plugin
    implementation(compose.desktop.macos_x64)
    implementation(compose.desktop.macos_arm64)
    implementation(compose.desktop.linux_x64)
    implementation(compose.desktop.linux_arm64)
    implementation(compose.desktop.windows_x64)
    implementation(compose.desktop.components.splitPane)
    implementation(compose.materialIconsExtended)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.datetime)

    // Ballast, to manage its own UI state (with debugger artifact to share serialization models between the client and server)
    api(libs.kotlinx.coroutines.swing)
    api(libs.multiplatformSettings.core)
    api(libs.multiplatformSettings.test)
    api(project(":ballast-core"))
    api(project(":ballast-repository"))
    api(project(":ballast-saved-state"))
    api(project(":ballast-navigation"))
    api(project(":ballast-debugger"))
    api(project(":ballast-debugger-server"))
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    pluginName.set("Ballast")
    type.set("IC")
    version.set("2022.3")
    downloadSources.set(true)
    plugins.set(listOf("org.jetbrains.kotlin"))
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
        untilBuild.set("231.*")
    }

    runPluginVerifier {
        ideVersions.set(listOf("2021.3", "2022.1", "2022.1.4", "2022.3"))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-opt-in=kotlin.ExperimentalStdlibApi"
        freeCompilerArgs += "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        freeCompilerArgs += "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        freeCompilerArgs += "-opt-in=androidx.compose.animation.ExperimentalAnimationApi"
        freeCompilerArgs += "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"
        freeCompilerArgs += "-opt-in=androidx.compose.material.ExperimentalMaterialApi"
        freeCompilerArgs += "-opt-in=org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi"
    }
}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
        topLevelConstants = true
    }

    buildConfigField("String", "BALLAST_VERSION", "\"${project.version}\"")
}
