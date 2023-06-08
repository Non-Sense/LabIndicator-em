import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "com.n0n5ense"
version = "1.0"

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.7.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.20")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}