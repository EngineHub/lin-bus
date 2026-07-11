import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    id("org.enginehub.crankcase.java")
    id("org.enginehub.crankcase.checkstyle")
    jacoco
}

crankcaseJava {
    javaRelease = 25
    disabledLints.add("module")
}

crankcaseCheckstyle {
    suppressionsFile = isolated.rootProject.projectDirectory.file("config/checkstyle/suppressions.xml")
}

tasks.withType<Checkstyle>().configureEach {
    // Checkstyle has no grammar for module declarations: checkstyle/checkstyle#8240
    exclude("**/module-info.java")
}

jacoco {
    toolVersion = "0.8.15"
}

val sourceSets = extensions.getByType(SourceSetContainer::class.java)
sourceSets["test"].resources.srcDir("../shared-test-resources")

val mainSourceSet = sourceSets["main"]
val jacocoExecData = fileTree(layout.buildDirectory.dir("jacoco")).include("*.exec")

val jacocoTestCoverageVerification =
    tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        violationRules {
            rule {
                limit {
                    minimum = "0.95".toBigDecimal()
                }
            }
        }

        executionData(jacocoExecData)
        sourceSets(mainSourceSet)
        dependsOn(tasks.named("test"))
        // Add this dependency, not because it's necessary, but because it helps when figuring out
        // what is wrong with the coverage
        dependsOn(tasks.named("jacocoTestReport"))
    }
tasks.named("check") {
    dependsOn(jacocoTestCoverageVerification)
}
