plugins {
    java
    application
    id("com.google.osdetector") version "1.7.0"
}

java {
     toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

application {
    mainClass.set("org.enginehub.linbus.gui.LinBusGui")
    mainModule.set("org.enginehub.linbus.gui")
}

repositories {
    maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        val injectApi = libs.jakarta.injectApi.get()
        substitute(module("javax.inject:javax.inject:1"))
            .using(module(
                "${injectApi.module.group}:${injectApi.module.name}:${injectApi.versionConstraint.requiredVersion}"
            ))
    }
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.polymerization.annotations)

    annotationProcessor(libs.dagger.compiler)
    annotationProcessor(libs.polymerization.processor)

    implementation(project(":core"))
    implementation(libs.dagger.core)
    implementation(libs.guava)

    for (lib in listOf(libs.javafx.base, libs.javafx.controls, libs.javafx.graphics)) {
        implementation(lib)
        implementation(variantOf(lib) {
            classifier(
                when (osdetector.os) {
                    "osx" -> "mac"
                    "windows" -> "win"
                    else -> "linux"
                }
            )
        })
    }

    implementation(platform(libs.ikonli.bom))
    implementation(libs.ikonli.javafx)
    implementation(libs.ikonli.fontawesome5)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(libs.truth) {
        exclude(group = "junit")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("--enable-preview")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-Dglass.disableThreadChecks=true", "--enable-preview")
}

tasks.named<JavaExec>("run").configure {
    jvmArgs("-Dglass.disableThreadChecks=true", "--enable-preview")
}
