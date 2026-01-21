plugins {
    id("copper-leaf-base")
    id("copper-leaf-targets")
    id("copper-leaf-tests")
//    id("copper-leaf-lint")
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
                api("org.jetbrains.exposed:exposed-core:1.0.0")
                api("org.jetbrains.exposed:exposed-jdbc:1.0.0")
                api("org.jetbrains.exposed:exposed-kotlin-datetime:1.0.0")
                api("org.jetbrains.exposed:exposed-json:1.0.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                api("org.postgresql:postgresql:42.7.7")
                api("com.mysql:mysql-connector-j:9.5.0")
                api("org.jetbrains.exposed:exposed-migration-core:1.0.0")
                api("org.jetbrains.exposed:exposed-migration-jdbc:1.0.0")
            }
        }
    }
}
