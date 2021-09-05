plugins {
    `java-library`
    `maven-publish`
    jacoco
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnlyApi(libs.jetbrains.annotations)
    compileOnlyApi(libs.checkerframework.qual)

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
