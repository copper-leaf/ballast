plugins {
    id("copper-leaf-base")
    id("copper-leaf-docs")
}

orchid {
    diagnose = true
}

val orchidServe by tasks
val orchidBuild by tasks
val orchidDeploy by tasks
val processOrchidResources by tasks

val exampleProjects = listOf(
    "web",
    "counter",
)

exampleProjects.forEach { exampleProjectName ->
    tasks.register("copyExample${exampleProjectName.capitalize()}Sources", Copy::class) {
        val sourceDir = project.rootDir.resolve("examples/$exampleProjectName/build/dist/js/productionExecutable")
        val destinationDir = project.projectDir.resolve("src/orchid/resources/assets/examples/$exampleProjectName")

        onlyIf { sourceDir.exists() }

        from(sourceDir)
        into(destinationDir)
    }
}

val copyExampleComposeWebSources by tasks.registering {
    exampleProjects.forEach {exampleProjectName ->
        dependsOn("copyExample${exampleProjectName.capitalize()}Sources")
    }
}
orchidServe.dependsOn(copyExampleComposeWebSources)
orchidBuild.dependsOn(copyExampleComposeWebSources)
orchidDeploy.dependsOn(copyExampleComposeWebSources)
processOrchidResources.mustRunAfter(copyExampleComposeWebSources)
