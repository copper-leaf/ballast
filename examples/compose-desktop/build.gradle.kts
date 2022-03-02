plugins {
    kotlin("multiplatform")
    `copper-leaf-base`
    `copper-leaf-version`
    `copper-leaf-lint`
    id("org.jetbrains.compose")
}

description = "Ballast example app using Jetbrains Compose for Desktop"

kotlin {
    jvm { }

    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlin.Experimental")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(":ballast-core"))
                implementation(compose.desktop.currentOs)
                implementation(compose.material)
                implementation(compose.uiTooling)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.copperleaf.ballast.compose.desktop.MainKt"
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
    kotlinOptions {
        jvmTarget = Config.javaVersion
    }
}
