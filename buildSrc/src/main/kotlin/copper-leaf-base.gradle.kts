plugins {
    base
}

repositories {
    mavenCentral()
    google()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

group = Config.groupId

var ghUser: String by project.extra
var ghToken: String by project.extra
var jetbrainsSpaceUser: String by project.extra
var jetbrainsSpaceToken: String by project.extra

ghUser              = loadProperty("githubUsername", "GITHUB_ACTOR")
ghToken             = loadProperty("githubToken", "GITHUB_TOKEN")
jetbrainsSpaceUser  = loadProperty("jetbrainsSpaceUser", "JETBRAINS_SPACE_USER")
jetbrainsSpaceToken = loadProperty("jetbrainsSpaceToken", "JETBRAINS_SPACE_TOKEN")
