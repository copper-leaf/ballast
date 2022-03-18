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
        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)
                implementation(project(":ballast-core"))
                implementation(project(":ballast-debugger"))
                implementation("io.ktor:ktor-client-js:1.6.7")
            }
        }
    }
}
