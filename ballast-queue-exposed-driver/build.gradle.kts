plugins {
    id("copper-leaf-base")
    id("copper-leaf-targets")
    id("copper-leaf-tests")
    id("copper-leaf-lint")
    id("copper-leaf-publish")
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                api(project(":ballast-queue-core"))
                api(libs.exposed.core)
                api(libs.exposed.jdbc)
                api(libs.exposed.kotlindatetime)
                api(libs.exposed.json)
                api(libs.exposed.migration.core)
                api(libs.exposed.migration.jdbc)
            }
        }
        val jvmTest by getting {
            dependencies {
                api(libs.jdbc.postgres)
                api(libs.jdbc.mysql)
                api(libs.testcontainers)
            }
        }
    }
}
