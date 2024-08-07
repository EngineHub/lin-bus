package org.enginehub.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

class JvmPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            apply<LicensePlugin>()
            apply(plugin = "java")
            repositories {
                mavenCentral()
            }
            configure<JavaPluginExtension> {
                toolchain.languageVersion.set(JavaLanguageVersion.of(21))
                withJavadocJar()
                withSourcesJar()
            }
            the<SourceSetContainer>()["test"].resources.srcDir("../shared-test-resources")
            tasks.withType<JavaCompile> {
                options.encoding = "UTF-8"
                options.isDeprecation = true
                options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all", "-Xlint:-module"))
            }
            tasks.named<Test>("test") {
                useJUnitPlatform()
            }
            tasks.named<Javadoc>("javadoc") {
                options.encoding = "UTF-8"
                (options as StandardJavadocDocletOptions).apply {
                    addBooleanOption("Werror", true)
                    tags(
                        "apiNote:a:API Note:",
                        "implSpec:a:Implementation Requirements:",
                        "implNote:a:Implementation Note:"
                    )
                }
                // Disable up-to-date check, it is wrong for javadoc with -Werror
                outputs.upToDateWhen { false }
            }
            configureJacoco()
        }
    }

    private fun Project.configureJacoco() {
        apply(plugin = "jacoco")
        val jacocoTestCoverageVerification =
            tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
                violationRules {
                    rule {
                        limit {
                            minimum = "0.95".toBigDecimal()
                        }
                    }
                }

                val src = project.the<JavaPluginExtension>().sourceSets["main"]
                val jacocoExecData = fileTree(project.layout.buildDirectory.dir("jacoco")).include("*.exec")
                executionData(jacocoExecData)
                sourceSets(src)
                dependsOn(project.tasks.named("test"))
                // Add this dependency, not because it's necessary, but because it helps when figuring out
                // what is wrong with the coverage
                dependsOn(project.tasks.named("jacocoTestReport"))
            }
        tasks.named("check") {
            dependsOn(jacocoTestCoverageVerification)
        }
    }
}
