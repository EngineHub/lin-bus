plugins {
    id("org.enginehub.lin-bus.java-library-conventions")
    alias(libs.plugins.crankcase.licensing)
    alias(libs.plugins.crankcase.publishing)
    alias(libs.plugins.jmh)
}

dependencies {
    compileOnlyApi(libs.jspecify.annotations)

    api(project(":common"))
    api(project(":stream"))

    testImplementation(libs.truth) {
        exclude(group = "junit")
    }
}

jmh {
    jmhVersion = libs.versions.jmh
    fork = 2
    warmupIterations = 3
    warmup = "1s"
    iterations = 5
    timeOnIteration = "1s"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "lin-bus-tree"
            from(components["java"])
        }
    }
}
