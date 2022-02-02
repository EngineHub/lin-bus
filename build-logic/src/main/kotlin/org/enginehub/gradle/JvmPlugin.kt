package org.enginehub.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*

class JvmPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            apply(plugin = "java")
            configure<JavaPluginExtension> {
                toolchain.languageVersion.set(JavaLanguageVersion.of(17))
                withJavadocJar()
                withSourcesJar()
            }
            the<SourceSetContainer>()["test"].resources.srcDir("../shared-test-resources")
            tasks.withType<JavaCompile> {
                options.compilerArgs.addAll(listOf("-parameters", "-Xlint:unchecked"))
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
        }
    }
}
