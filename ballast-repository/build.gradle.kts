plugins {
    `copper-leaf-android`
    `copper-leaf-targets`
    `copper-leaf-base`
    `copper-leaf-version`
    `copper-leaf-lint`
    `copper-leaf-publish`
}

description = "Opinionated Kotlin multiplatform Repository Caching library based on Ballast MVI"

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":ballast-api"))
                implementation(project(":ballast-viewmodel"))
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
