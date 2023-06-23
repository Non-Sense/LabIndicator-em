import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "com.n0n5ense"
version = "1.0"

dependencies {
    implementation(project(":database"))
    implementation(project(":common"))

    implementation("net.dv8tion:JDA:5.0.0-beta.2")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("org.mindrot:jbcrypt:0.4")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.10")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}