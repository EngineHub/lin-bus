plugins {
    `java-library`
}

java {
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
