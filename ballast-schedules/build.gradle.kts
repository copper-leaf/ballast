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
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":ballast-api"))
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.serialization)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":ballast-test"))
            }
        }

        val jvmMain by getting {
            dependencies { }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.work:work-runtime-ktx:2.8.1")
            }
        }
        val jsMain by getting {
            dependencies { }
        }
        val iosMain by getting {
            dependencies { }
        }
    }
}
