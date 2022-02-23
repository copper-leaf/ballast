@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    `copper-leaf-base`
    `copper-leaf-version`
    `copper-leaf-lint`
    id("org.jetbrains.compose")
    id("com.github.gmazzo.buildconfig")
}

var projectVersion: ProjectVersion by project.extra

description = "Debugger UI application for Ballast MVI"
// version = projectVersion.releaseVersion
version = "1.0.0"

dependencies {
    // compose Desktop app
    implementation(compose.desktop.currentOs)
    implementation(compose.desktop.components.splitPane)
    implementation(compose.desktop.components.splitPane)
    implementation(compose.materialIconsExtended)

    // Ktor websocker server
    implementation("io.ktor:ktor-server-core:1.6.5")
    implementation("io.ktor:ktor-server-cio:1.6.5")
    implementation("io.ktor:ktor-websockets:1.6.5")
    implementation("ch.qos.logback:logback-classic:1.2.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")

    // Ballast, to manage its own UI state (with debugger artifact to share serialization models between the client and server)
    implementation(project(":ballast-core"))
    implementation(project(":ballast-debugger"))
}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
        topLevelConstants = true
    }

    buildConfigField("String", "BALLAST_VERSION", "\"${projectVersion}\"")
}

compose.desktop {
    application {
        mainClass = "com.copperleaf.ballast.debugger.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            copyright = "© 2022 Copper Leaf. All rights reserved."

            macOS {
                bundleID = "${project.group}.app"
                packageName = "Ballast Debugger"
            }
            windows {
                packageName = "Ballast Debugger"
            }

            modules(
                "java.instrument",
                "java.management",
                "java.naming",
                "java.sql",
                "jdk.unsupported",
            )
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
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.useIR = true
    kotlinOptions {
        jvmTarget = Config.javaVersion
    }
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xopt-in=kotlin.RequiresOptIn",
        "-Xopt-in=kotlin.ExperimentalStdlibApi",
        "-Xopt-in=kotlin.time.ExperimentalTime",
        "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    )
}
