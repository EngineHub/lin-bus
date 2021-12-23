plugins {
    `java-library`
    `maven-publish`
    jacoco
    id("org.enginehub.lin-bus.jvm")
}

dependencies {
    compileOnlyApi(libs.jetbrains.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)

    testImplementation(libs.truth) {
        exclude(group = "junit")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "linbus-core"
            from(components["java"])
        }
    }
}
