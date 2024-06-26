plugins {
    `java-library`
    jacoco
    id("org.enginehub.lin-bus.jvm")
    id("org.enginehub.lin-bus.publishing")
}

dependencies {
    compileOnlyApi(libs.jspecify.annotations)

    api(project(":common"))
    api(project(":stream"))

    testImplementation(project(":tree"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(libs.truth) {
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
