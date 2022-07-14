plugins {
    `java-library`
    jacoco
    id("org.enginehub.lin-bus.jvm")
    id("org.enginehub.lin-bus.publishing")
}

dependencies {
    compileOnlyApi(libs.jetbrains.annotations)

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
            artifactId = "lin-bus-common"
            from(components["java"])
        }
    }
}
