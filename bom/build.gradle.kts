plugins {
    `java-platform`
    alias(libs.plugins.crankcase.publishing)
}

dependencies {
    constraints {
        for (projectName in listOf("common", "format-snbt", "stream", "tree")) {
            api(project(":$projectName"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "lin-bus-bom"
            from(components["javaPlatform"])
        }
    }
}
