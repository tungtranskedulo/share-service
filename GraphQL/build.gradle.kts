plugins {
    id("java")
    id("org.springframework.boot") version "2.6.7"
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    // spring
    implementation("org.springframework.boot:spring-boot-starter-web")

    // rate limiter
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")

    implementation("jakarta.annotation:jakarta.annotation-api:1.3.5")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.0")
    implementation("io.arrow-kt:arrow-core-data:0.12.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    val kotestVersion = "4.4.3"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-spring:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-arrow:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
   // testImplementation("io.kotest:kotest-assertions-shared-jvm:$kotestVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}