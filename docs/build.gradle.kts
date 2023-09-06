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

val copyExampleComposeWebSources by tasks.registering(Copy::class) {
    from(project.rootDir.resolve("examples/web/build/dist/js/productionExecutable"))
    into(project.projectDir.resolve("src/orchid/resources/assets/example/distributions"))
}
orchidServe.dependsOn(copyExampleComposeWebSources)
orchidBuild.dependsOn(copyExampleComposeWebSources)
orchidDeploy.dependsOn(copyExampleComposeWebSources)
processOrchidResources.mustRunAfter(copyExampleComposeWebSources)
