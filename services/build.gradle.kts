plugins {
    id("java")
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "org.example"
version = "unspecified"

sourceSets {
    create("educative") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        // add test source so int test can reuse
        compileClasspath += sourceSets.test.get().output
        runtimeClasspath += sourceSets.test.get().output
    }
}

springBoot {
    mainClass.set("com.share.Application")
}

tasks.getByName<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    args = listOf("--spring.profiles.active=common-dev")
}

dependencies {
    // spring
    implementation("org.springframework.boot:spring-boot-starter")
    //implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // fasterxml
    implementation("jakarta.annotation:jakarta.annotation-api:1.3.5")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Swagger API Documentation
    implementation("org.springdoc:springdoc-openapi-ui:1.6.8")
    implementation("org.springdoc:springdoc-openapi-webmvc-core:1.6.8")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.8")
    implementation("org.springdoc:springdoc-openapi-common:1.6.8")

    // AOP
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // rate limiter
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")
    implementation("io.github.resilience4j:resilience4j-retry:1.7.0")

    // jetbrains
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")

    //log
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")

    //cache
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.3")

    // others
    implementation("io.arrow-kt:arrow-core-data:0.12.1")
    implementation("org.openjdk.jmh:jmh-core:1.35")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.35")

    val kotestVersion = "4.4.3"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-spring:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-arrow:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

