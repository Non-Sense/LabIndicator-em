plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    application
}

group = "com.n0n5ense"
version = "0.1.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {
    implementation(project(":bot"))
    implementation(project(":server"))
    implementation(project(":database"))
    implementation(project(":common"))

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
}

application {
    mainClass.value("MainKt")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "MainKt"
        attributes["Multi-Release"] = true
    }
}