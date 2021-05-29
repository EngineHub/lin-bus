import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    jacoco
    id("org.cadixdev.licenser") version "0.6.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.cadixdev.licenser")

    configure<LicenseExtension> {
        exclude {
            it.file.startsWith(project.buildDir)
        }
        header(rootProject.file("HEADER.txt"))
        (this as ExtensionAware).extra.apply {
            for (key in listOf("organization", "url")) {
                set(key, rootProject.property(key))
            }
        }
    }

    plugins.withId("java") {
        configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        }
    }
}
