plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation("com.android.tools.build:gradle:7.0.3")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:10.2.1")
    implementation("org.jetbrains.compose:compose-gradle-plugin:1.1.0")
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.6.10")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.github.gmazzo:gradle-buildconfig-plugin:3.0.3")
    implementation("org.jetbrains.intellij.plugins:gradle-intellij-plugin:1.4.0")
    implementation("org.jetbrains.intellij.plugins:gradle-changelog-plugin:1.3.1")
}
