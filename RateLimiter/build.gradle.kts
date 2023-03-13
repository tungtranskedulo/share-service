plugins {
    id("java")
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    // rate limiter
    //implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    //cache
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.google.guava:guava:30.1.1-jre")
//    implementation("com.github.ben-manes.caffeine:caffeine:3.0.3")
//    implementation("com.github.ben-manes.caffeine:guava:3.0.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}