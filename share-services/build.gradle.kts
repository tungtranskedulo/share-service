import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    idea
    id("org.springframework.boot") version "2.7.8"
    id("com.github.johnrengelman.processes") version "0.5.0"
    id("com.gorylenko.gradle-git-properties") version "2.2.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

springBoot {
    mainClass.set("com.share.services.Application")
}

tasks.getByName<BootRun>("bootRun") {
    args = listOf("--spring.profiles.active=common-dev")
}

configurations {
    create("standaloneJars")
}

dependencies {
    // spring
    //annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework:spring-web:6.0.0")
//    implementation("org.springframework.boot:spring-boot-starter-actuator")
//    implementation("org.springframework.boot:spring-boot-starter-cache")
//    implementation("org.springframework.boot:spring-boot-starter-webflux")
//    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-autoconfigure:2.7.8")

    // rate limiter
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")

    // jetbrains kotlin
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.5.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")

    // AOP
    implementation("org.aspectj:aspectjweaver:1.9.7")

    //log
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
    implementation("org.yaml:snakeyaml:1.33") // todo do not update
    implementation("org.springframework.boot:spring-boot-starter-log4j2")

    //cache
    implementation("com.google.guava:guava:30.1.1-jre")
//    implementation("com.github.ben-manes.caffeine:caffeine:3.0.3")
//    implementation("com.github.ben-manes.caffeine:guava:3.0.3")

    // others
    implementation("jakarta.annotation:jakarta.annotation-api:1.3.5")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.0")
    implementation("io.arrow-kt:arrow-core-data:0.12.1")
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
   // testImplementation("org.springframework.boot:spring-boot-starter-test")
}
