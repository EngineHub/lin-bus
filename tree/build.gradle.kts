plugins {
    `java-library`
    `maven-publish`
    jacoco
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnlyApi(libs.jetbrains.annotations)

    implementation(project(":common"))
    implementation(project(":stream"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(libs.truth) {
        exclude(group = "junit")
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "linbus-core"
            from(components["java"])
        }
    }
}
