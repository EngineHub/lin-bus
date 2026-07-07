plugins {
    application
    id("org.enginehub.lin-bus.java-conventions")
    alias(libs.plugins.crankcase.licensing)
    alias(libs.plugins.osdetector)
}

crankcaseJava {
    // guava is an automatic module
    disabledLints.add("requires-automatic")
    // guava's class files reference Error Prone annotations not on the module graph
    disabledLints.add("classfile")
}

application {
    mainModule.set("org.enginehub.linbus.gui")
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
            classifier(
                when (osdetector.os) {
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
            )
        })
    }

    implementation(platform(libs.ikonli.bom))
    implementation(libs.ikonli.javafx)
    implementation(libs.ikonli.fontawesome5)

    testImplementation(libs.truth) {
        exclude(group = "junit")
    }
}

tasks.compileJava {
    options.javaModuleMainClass = "org.enginehub.linbus.gui.LinBusGui"
}

tasks.javadoc {
    (options as CoreJavadocOptions).addBooleanOption("Xdoclint:-missing").value = true
}

tasks.named<JavaExec>("run") {
    // Allow scenic-view hooking
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}
