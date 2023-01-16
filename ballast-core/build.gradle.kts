plugins {
    `copper-leaf-android`
    `copper-leaf-targets`
    `copper-leaf-base`
    `copper-leaf-version`
    `copper-leaf-lint`
    `copper-leaf-publish`
}

description = "Opinionated Application State Management framework for Kotlin Multiplatform"

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":ballast-api"))
                api(project(":ballast-viewmodel"))
                api(project(":ballast-logging"))
                api(project(":ballast-utils"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":ballast-test"))
                api(libs.kotlinx.coroutines.test)
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
