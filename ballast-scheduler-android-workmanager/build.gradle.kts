plugins {
    id("copper-leaf-base")
    id("copper-leaf-android-library")
    id("copper-leaf-targets")
    id("copper-leaf-tests")
    id("copper-leaf-lint")
    id("copper-leaf-publish")
    id("copper-leaf-serialization")
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }

    sourceSets {
        val androidMain by getting {
            dependencies {
                api(libs.androidx.workmanager)
                implementation(project(":ballast-scheduler-core"))
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(project(":ballast-test"))
            }
        }
    }
}
