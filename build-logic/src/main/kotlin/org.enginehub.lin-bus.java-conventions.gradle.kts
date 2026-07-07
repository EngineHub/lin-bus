import org.gradle.api.tasks.SourceSetContainer
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    id("org.enginehub.crankcase.java")
}

crankcaseJava {
    javaRelease = 21
    disabledLints.add("module")
}

val sourceSets = extensions.getByType(SourceSetContainer::class.java)
sourceSets["test"].resources.srcDir("../shared-test-resources")

apply(plugin = "jacoco")

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
