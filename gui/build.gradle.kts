import org.enginehub.gradle.b2.B2Upload

plugins {
    java
    application
    id("com.google.osdetector") version "1.7.0"
    id("org.beryx.jlink") version "3.0.1"
    id("org.enginehub.lin-bus.jvm")
    id("org.enginehub.lin-bus.publishing")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

application {
    mainModule.set("org.enginehub.linbus.gui")
}

repositories {
    maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
}

val classifierValue = when (osdetector.os) {
    "osx" -> when (osdetector.arch) {
        "x86_64" -> "mac"
        "aarch_64" -> "mac-aarch64"
        else -> error("Unsupported architecture: ${osdetector.arch}")
    }

    "windows" -> when (osdetector.arch) {
        "x86_64" -> "win"
        else -> error("Unsupported architecture: ${osdetector.arch}")
    }

    "linux" -> when (osdetector.arch) {
        "x86_64" -> "linux"
        "aarch_64" -> "linux-aarch64"
        else -> error("Unsupported architecture: ${osdetector.arch}")
    }

    else -> error("Unsupported OS: ${osdetector.os}")
}

dependencies {
    compileOnly(libs.jspecify.annotations)

    implementation(project(":tree"))
    implementation(libs.guava)
    implementation(libs.tinylog.api)
    runtimeOnly(libs.tinylog.impl)
    runtimeOnly(libs.tinylog.slf4j.impl)

    for (lib in listOf(libs.javafx.base, libs.javafx.controls, libs.javafx.graphics)) {
        implementation(lib)
        implementation(variantOf(lib) {
            classifier(classifierValue)
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

val mainClassValue = "org.enginehub.linbus.gui.LinBusGui"

tasks.compileJava {
    options.javaModuleMainClass = mainClassValue
}

tasks.javadoc {
    (options as CoreJavadocOptions).addBooleanOption("Xdoclint:-missing").value = true
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    // Allow scenic-view hooking
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

// Separate version system since packages require it to be major.minor.build only
val runNumber = providers.environmentVariable("GITHUB_RUN_NUMBER")
    .orElse("0")
    .map { it.toInt() }
    .get()
val appVersionValue = "1.0.$runNumber"

jlink {
    moduleName = "org.enginehub.linbus.gui"
    mainClass = mainClassValue
    options = listOf(
        "--strip-debug",
        "--compress=zip-9",
        "--no-header-files",
        "--no-man-pages"
    )
    launcher {
        name = "lin-bus"
    }
    jpackage {
        options = listOf("--verbose")
        installerOptions = listOf("--verbose")
        appVersion = appVersionValue
        installerOutputDir = layout.buildDirectory.dir("installers").get().asFile
    }
}

tasks.register<B2Upload>("uploadDistributions") {
    dependsOn(tasks.jpackage)
    inputDir = tasks.jpackage.map { it.jpackageData.installerOutputDir }
    bucketName = providers.environmentVariable("B2_BUCKET_NAME")
    prefix = providers.environmentVariable("B2_PREFIX").map { "$it/$appVersionValue/$classifierValue" }
}
