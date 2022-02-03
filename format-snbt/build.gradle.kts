plugins {
    `java-library`
    `maven-publish`
    jacoco
    id("org.enginehub.lin-bus.jvm")
}

dependencies {
    compileOnlyApi(libs.jetbrains.annotations)

    api(project(":common"))
    api(project(":stream"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(libs.truth.asProvider()) {
        exclude(group = "junit")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "${rootProject.group}.format"
            artifactId = "lin-bus-format-snbt"
            from(components["java"])
        }
    }
}
