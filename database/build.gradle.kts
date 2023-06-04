import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("kapt") version "1.7.10"
}

group = "com.n0n5ense"
version = "1.0"

dependencies {

    implementation(project(":common"))

    val exposedVersion = "0.41.1"

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.xerial:sqlite-jdbc:3.40.1.0")

    implementation("com.fasterxml.uuid:java-uuid-generator:4.2.0")


    val krushVersion = "1.2.1"
    api("pl.touk.krush:krush-annotation-processor:$krushVersion")
    kapt("pl.touk.krush:krush-annotation-processor:$krushVersion")
    api("pl.touk.krush:krush-runtime:$krushVersion")
    api("org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1.0.2.Final")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.10")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}