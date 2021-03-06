import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val junitJupiterVersion = "5.7.2"
val jacksonVersion = "2.12.3"
val ulidVersion = "8.2.0"

plugins {
    kotlin("jvm") version "1.5.20"
}

repositories {
    mavenCentral()
}

allprojects {
    group = "no.nav.k9.rapid"
    version = properties["version"] ?: "local-build"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))

        api("de.huxhorn.sulky:de.huxhorn.sulky.ulid:$ulidVersion")

        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showExceptions = true
            showStackTraces = true
            showCauses = true
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
        }
    }
}