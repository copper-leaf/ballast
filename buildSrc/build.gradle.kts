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
    implementation("com.android.tools.build:gradle:7.2.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    implementation("org.jetbrains.compose:compose-gradle-plugin:1.2.0-alpha01-dev745")
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.7.10")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:11.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.github.gmazzo:gradle-buildconfig-plugin:3.1.0")
    implementation("org.jetbrains.intellij.plugins:gradle-intellij-plugin:1.4.0")
    implementation("org.jetbrains.intellij.plugins:gradle-changelog-plugin:1.3.1")
}
tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}
