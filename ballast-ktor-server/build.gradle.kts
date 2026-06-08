plugins {
    id("copper-leaf-base")
    id("copper-leaf-targets")
    id("copper-leaf-tests")
    id("copper-leaf-lint")
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
            dependencies {
                implementation(libs.ktor.server.core)
            }
        }
    }
}
