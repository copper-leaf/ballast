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
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.datetime)
            }
        }
        val commonTest by getting {
            dependencies { }
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
        val jsTest by getting {
            dependencies {
                implementation(npm("@js-joda/timezone", "2.22.0"))
            }
        }
        val wasmJsTest by getting {
            dependencies {
                implementation(npm("@js-joda/timezone", "2.22.0"))
            }
        }
        val iosMain by getting {
            dependencies { }
        }
    }
}
