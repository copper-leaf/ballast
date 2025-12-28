plugins {
    id("copper-leaf-base")
    id("copper-leaf-compatibility")
    id("copper-leaf-sonatype")
}

apiValidation {
    ignoredProjects.addAll(
        listOf(
//            "docs",
            "android",
            "counter",
            "desktop",
            "navigationWithCustomRoutes",
            "navigationWithEnumRoutes",
            "schedules",
            "web",
            "ballast-idea-plugin",
        )
    )
}
