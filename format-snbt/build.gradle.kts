plugins {
    id("org.enginehub.lin-bus.java-library-conventions")
    alias(libs.plugins.crankcase.licensing)
    alias(libs.plugins.crankcase.publishing)
}

dependencies {
    compileOnlyApi(libs.jspecify.annotations)

    api(project(":common"))
    api(project(":stream"))

    testImplementation(project(":tree"))

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
