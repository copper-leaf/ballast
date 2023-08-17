import com.copperleaf.gradle.projectVersion

plugins {
    id("copper-leaf-base")
    id("copper-leaf-android-library")
    id("copper-leaf-targets")
    id("copper-leaf-buildConfig")
    id("copper-leaf-serialization")
    id("copper-leaf-kotest")
//    id("copper-leaf-lint")
    id("copper-leaf-publish")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":ballast-api"))
                implementation(project(":ballast-debugger-models"))

                implementation(libs.bundles.ktorClient)
                implementation(libs.kotlinx.datetime)
                implementation(libs.benasher44.uuid)
            }
        }
        val jvmMain by getting {
            dependencies { }
        }
        val androidMain by getting {
            dependencies { }
        }
        val jsMain by getting {
            dependencies { }
        }
        val iosMain by getting {
            dependencies { }
        }
    }
}

buildConfig {
    projectVersion(project, "BALLAST_VERSION")
}
