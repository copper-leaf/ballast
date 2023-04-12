pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

val conventionDir = "./gradle-convention-plugins"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("$conventionDir/gradle/conventionLibs.versions.toml"))
        }
    }
}

includeBuild(conventionDir)

rootProject.name = "ballast"

include(":ballast-api")
include(":ballast-viewmodel")
include(":ballast-logging")
include(":ballast-utils")
include(":ballast-core")

include(":ballast-repository")
include(":ballast-saved-state")
include(":ballast-debugger")
include(":ballast-sync")
include(":ballast-undo")
include(":ballast-navigation")

include(":ballast-crash-reporting")
include(":ballast-analytics")

include(":ballast-debugger-server")
include(":ballast-idea-plugin")

include(":ballast-test")

include(":examples:android")
include(":examples:desktop")
include(":examples:web")

include(":docs")
