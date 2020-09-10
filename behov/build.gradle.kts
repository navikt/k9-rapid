plugins {
    id("java")
    id("maven-publish")
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
                name.set("behov")
                description.set("Behovsverktøy")
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
