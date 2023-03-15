import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
  // Mokito failed when updated to kotlin 1.8.0, see https://youtrack.jetbrains.com/issue/KT-55125
  // Should wating for kotlin 1.8.20 release
  val kotlinVersion = "1.7.10"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  kotlin("jvm") version kotlinVersion
  kotlin("plugin.spring") version kotlinVersion
}
java.sourceCompatibility = JavaVersion.VERSION_11

allprojects {
  repositories {
    mavenCentral()
    //maven(url = "https://mobile.maven.couchbase.com/maven2/dev/")
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
