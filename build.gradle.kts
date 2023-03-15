import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  val kotlinVersion = "1.7.10"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  kotlin("jvm") version kotlinVersion
  kotlin("plugin.spring") version kotlinVersion
}

apply(plugin = "io.spring.dependency-management")
apply(plugin = "jacoco")

java.sourceCompatibility = JavaVersion.VERSION_11

allprojects {
  repositories {
    mavenCentral()
    maven(url = "https://mobile.maven.couchbase.com/maven2/dev/")
  }

  tasks {
    withType<Test> {
      useJUnitPlatform()
    }

    withType<KotlinCompile> {
      kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xemit-jvm-type-annotations")
        jvmTarget = "11"
        allWarningsAsErrors = true
      }
    }

  }
}

