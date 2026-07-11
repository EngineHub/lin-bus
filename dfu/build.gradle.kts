plugins {
    id("org.enginehub.lin-bus.java-library-conventions")
    alias(libs.plugins.crankcase.licensing)
    alias(libs.plugins.crankcase.publishing)
}

dependencies {
    compileOnlyApi(libs.jspecify.annotations)

    api(project(":tree"))
    api(libs.datafixerupper)

    testImplementation(libs.truth) {
        exclude(group = "junit")
    }
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.enginehub.linbus.dfu")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "lin-bus-dfu"
            from(components["java"])
        }
    }
}
