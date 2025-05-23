plugins {
    id("java")
    id("maven-publish")
}

dependencies {
    api(project(":behov"))
    testImplementation("org.skyscreamer:jsonassert:1.5.3")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/k9-rapid")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {

            pom {
                name.set("fordele-omsorgsdager")
                description.set("Behov og Løsning på søknad om å fordele omsorgsdager")
                url.set("https://github.com/navikt/k9-rapid")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/navikt/k9-rapid.git")
                    developerConnection.set("scm:git:https://github.com/navikt/k9-rapid.git")
                    url.set("https://github.com/navikt/k9-rapid")
                }
            }
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}
