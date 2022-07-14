plugins {
    `java-library`
    jacoco
    id("org.enginehub.lin-bus.jvm")
    id("org.enginehub.lin-bus.publishing")
}

dependencies {
    compileOnlyApi(libs.jetbrains.annotations)

    api(project(":common"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(libs.truth.asProvider()) {
        exclude(group = "junit")
    }
    testImplementation(libs.truth.extensions.java8) {
        exclude(group = "junit")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "lin-bus-stream"
            from(components["java"])
        }
    }
}
