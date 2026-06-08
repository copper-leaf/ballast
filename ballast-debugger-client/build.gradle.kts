import com.copperleaf.gradle.projectVersion

plugins {
    id("copper-leaf-base")
    id("copper-leaf-android-library")
    id("copper-leaf-targets")
    id("copper-leaf-buildConfig")
    id("copper-leaf-serialization")
    id("copper-leaf-tests")
    id("copper-leaf-lint")
    id("copper-leaf-publish")
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":ballast-api"))
                api(project(":ballast-debugger-models"))

                implementation(libs.bundles.ktorClient)
                implementation(libs.kotlinx.datetime)
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

buildConfig {
    projectVersion(project, "BALLAST_VERSION")
    packageName.set("io.github.copperleaf.ballastdebuggerclient")
}

// tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    compilerOptions {
// //        jvmTarget.set(ConventionConfig.repoInfo(project).javaVersion)
// //        freeCompilerArgs.add("-opt-in=kotlin.ExperimentalStdlibApi")
// //        freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
// //        freeCompilerArgs.add("-opt-in=androidx.compose.foundation.ExperimentalFoundationApi")
// //        freeCompilerArgs.add("-opt-in=androidx.compose.animation.ExperimentalAnimationApi")
// //        freeCompilerArgs.add("-opt-in=androidx.compose.ui.ExperimentalComposeUiApi")
// //        freeCompilerArgs.add("-opt-in=androidx.compose.material.ExperimentalMaterialApi")
// //        freeCompilerArgs.add("-opt-in=org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi")
// //        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
//        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
//    }
// }
