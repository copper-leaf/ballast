plugins {
    id("copper-leaf-base")
    id("copper-leaf-android-library")
    id("copper-leaf-targets")
    id("copper-leaf-tests")
//    id("copper-leaf-lint")
    id("copper-leaf-publish")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":ballast-api"))
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
        val iosMain by getting {
            dependencies { }
        }
    }
}
