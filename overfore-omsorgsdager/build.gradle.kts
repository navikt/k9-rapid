plugins {
    id("java")
    id("maven-publish")
}

dependencies {
    implementation(project(":behov"))
    implementation(project(":losning"))
}
