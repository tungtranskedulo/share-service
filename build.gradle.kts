import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Gradle build script section below.
 */
buildscript {

  repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }

  }

  dependencies {
  }
}

/**
 * Gradle repositories section below.
 */
repositories {
  mavenCentral()
  maven { url = uri("https://repo.spring.io/milestone") }
  maven { url = uri("https://repo.spring.io/snapshot") }
}

/**
 * Script variables section below.
 */
val applicationName: String by project
val projectName: String by project

val kotlinVersion = "1.6.10"
val kotlinxVersion = "1.6.1"
val jettyVersion = "8.1.17.v20150415"
val springCloudVersion =
  "2021.0.1" // Release Train = 2021.0.x aka Jubilee; Boot Version: 2.6.x (Starting with 2021.0.1)

java.sourceCompatibility = JavaVersion.VERSION_11

/**
 * Gradle dependencies section below.
 */
dependencies {
  implementation(project(":RateLimiter"))

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  testImplementation("org.springframework.boot:spring-boot-starter-test")

}

configurations {
  all {
    exclude(group = "org.jboss.spec", module = "jboss-javaee-7.0")
    exclude(group = "org.jboss.spec.javax.servlet", module = "jboss-servlet-api_3.1_spec")
  }
}

/**
 * Plugins and plugin extensions section below
 */
plugins {
  id("checkstyle")
  id("java")
  id("jacoco")
  id("java-library")
  id("org.springframework.boot") version "2.6.7"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  id("org.jetbrains.kotlin.plugin.noarg") version "1.6.10"

  kotlin("jvm") version "1.6.10"
  kotlin("plugin.spring") version "1.6.10"
}

apply(plugin = "io.spring.dependency-management")
apply(plugin = "jacoco")

/**
 * checkstyle{} extension below is used for checkstyle plugin.
 */
checkstyle {
  toolVersion = "8.32"
  config = resources.text.fromFile("gradle/config/checkstyle.xml")
}

configure<SourceSetContainer> {
  named("main") {
    java.srcDir("src/main/java")
  }
}

/**
 * Gradle task configurations section below.
 */
tasks.withType<Checkstyle> {
  // don't need to run checkStyle
  enabled = false
  reports {
    xml.required.set(true)
    html.required.set(true)
  }

  doLast {
    logger.lifecycle("Checkstyle report for module \"${project.name}\" is available here at file://${project.buildDir}/reports/checkstyle/main.html")
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf(
      "-Xjsr305=strict",
      "-opt-in=kotlin.RequiresOptIn",
      "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    )
    jvmTarget = "11"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

noArg {
  invokeInitializers = true
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

jacoco {
  toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
  dependsOn(tasks.test) // tests are required to run before generating the report
  reports {
    xml.required.set(true)
    csv.required.set(true)
    html.required.set(true)
    html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
  }

  classDirectories.setFrom(files(classDirectories.files.map {
    fileTree(it).apply {
      exclude("**/jooq/**") // All JOOQ generated classes
      exclude("**/configuration/**") // startup configurations)
    }
  }))
}

dependencyManagement {
  imports {
    "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
  }
}

