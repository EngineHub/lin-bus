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
            artifactId = "lin-bus-tree"
            from(components["java"])
        }
    }
}
