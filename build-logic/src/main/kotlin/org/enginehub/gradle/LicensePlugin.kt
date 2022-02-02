package org.enginehub.gradle

import org.cadixdev.gradle.licenser.LicenseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra

class LicensePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
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
    }
}
