import com.copperleaf.gradle.projectVersion

plugins {
    id("copper-leaf-base")
    id("copper-leaf-targets")
    id("copper-leaf-kotest")
    id("copper-leaf-serialization")
    id("copper-leaf-buildConfig")
//    id("copper-leaf-lint")
    id("copper-leaf-publish")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.datetime)
                implementation(libs.bundles.ktorEmbeddedServer)

                // Ballast, to manage its own UI state (with debugger artifact to share serialization models between the client and server)
                implementation(project(":ballast-core"))
                implementation(project(":ballast-saved-state"))
                implementation(project(":ballast-debugger"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":ballast-test"))
            }
        }
    }
}

buildConfig {
    projectVersion(project, "BALLAST_VERSION")
}
