plugins {
    id("com.eden.orchidPlugin")
    `copper-leaf-base`
    `copper-leaf-version`
    `copper-leaf-lint`
}

repositories {
    jcenter()
}

dependencies {
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidDocs:0.21.1")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidCopper:0.21.1")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidGithub:0.21.1")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidKotlindoc:0.21.1")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidPluginDocs:0.21.1")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidSnippets:0.21.1")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidWritersBlocks:0.21.1")
}

// Orchid setup
// ---------------------------------------------------------------------------------------------------------------------

val publishConfiguration: PublishConfiguration = Config.publishConfiguration(project)

orchid {
    githubToken = publishConfiguration.githubToken
    version = Config.projectVersion(project).documentationVersion
    diagnose = true
}

val build by tasks
val check by tasks
val orchidServe by tasks
val orchidBuild by tasks
val orchidDeploy by tasks

orchidBuild.mustRunAfter(check)
build.dependsOn(orchidBuild)

val copyExampleComposeWebSources by tasks.registering(Copy::class) {
    from(project.rootDir.resolve("examples/web/build/distributions"))
    into(project.projectDir.resolve("src/orchid/resources/assets/example/distributions"))
}
orchidServe.dependsOn(copyExampleComposeWebSources)
orchidBuild.dependsOn(copyExampleComposeWebSources)
orchidDeploy.dependsOn(copyExampleComposeWebSources)

val publish by tasks.registering {
    dependsOn(orchidDeploy)
}
