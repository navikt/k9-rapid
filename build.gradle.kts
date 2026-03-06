import org.gradle.api.tasks.testing.logging.TestExceptionFormat

val junitVersion = "6.0.3"
val jacksonVersion = "2.21.0"
val ulidVersion = "8.3.0"

plugins {

    kotlin("jvm") version "2.3.10"
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
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/navikt/rapids-and-rivers")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: "x-access-token"
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))

        api("de.huxhorn.sulky:de.huxhorn.sulky.ulid:$ulidVersion")

        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

        testImplementation("org.junit.platform:junit-platform-launcher:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
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
