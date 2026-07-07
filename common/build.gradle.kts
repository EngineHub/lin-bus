plugins {
    id("org.enginehub.lin-bus.java-library-conventions")
    alias(libs.plugins.crankcase.licensing)
    alias(libs.plugins.crankcase.publishing)
}

dependencies {
    compileOnlyApi(libs.jspecify.annotations)

    testImplementation(libs.truth) {
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
