import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    base
    jacoco
    id("org.cadixdev.licenser") version "0.6.1" apply false
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
}

val jacocoTotalReport = tasks.register<JacocoReport>("jacocoTotalReport") {
    reports {
        xml.required.set(true)
        xml.outputLocation.set(rootProject.buildDir.resolve("reports/jacoco/report.xml"))
        html.required.set(true)
    }
}

val jacocoTestCoverageVerification = tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    violationRules {
        rule {
            limit {
                minimum = "0.95".toBigDecimal()
            }
        }
    }
}

subprojects.forEach { proj ->
    proj.plugins.withId("java") {
        proj.plugins.withId("jacoco") {
            val src = proj.the<JavaPluginExtension>().sourceSets["main"]
            val jacocoExecData = fileTree(proj.buildDir.absolutePath + "/jacoco").include("*.exec")
            listOf(jacocoTotalReport, jacocoTestCoverageVerification).forEach {
                it.configure {
                    executionData(jacocoExecData)
                    sourceSets(src)
                    dependsOn(proj.tasks.named("test"))
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(jacocoTotalReport, jacocoTestCoverageVerification)
}
