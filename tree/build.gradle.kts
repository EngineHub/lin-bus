plugins {
    id("org.enginehub.lin-bus.java-library-conventions")
    alias(libs.plugins.crankcase.licensing)
    alias(libs.plugins.crankcase.publishing)
}

dependencies {
    compileOnlyApi(libs.jspecify.annotations)

    api(project(":common"))
    api(project(":stream"))

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
