@file:Suppress("UnstableApiUsage")

import com.copperleaf.gradle.projectVersion

plugins {
    id("copper-leaf-base")
    id("copper-leaf-android-application")
    id("copper-leaf-targets")
    id("copper-leaf-tests")
//    id("copper-leaf-lint")
    id("copper-leaf-buildConfig")
}

android {
    buildTypes {
        val release by getting {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

kotlin { 
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":ballast-core"))
                implementation(project(":ballast-repository"))
                implementation(project(":ballast-saved-state"))
                implementation(project(":ballast-debugger-client"))
                implementation(project(":ballast-sync"))
                implementation(project(":ballast-undo"))
                implementation(project(":ballast-navigation"))

                implementation(libs.androidx.core)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.material)
                implementation(libs.androidx.fragment)
                implementation(libs.androidx.lifecycle.runtime)

                implementation(libs.bundles.ktorClient)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.multiplatformSettings.core)
                implementation(libs.multiplatformSettings.noArg)
                implementation(libs.coil)
            }
        }
    }
}

buildConfig {
    projectVersion(project, "BALLAST_VERSION")
}
