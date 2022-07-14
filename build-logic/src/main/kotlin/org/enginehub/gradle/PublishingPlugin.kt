package org.enginehub.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

private const val ARTIFACTORY_CONTEXT_URL = "artifactory_contextUrl"
private const val ARTIFACTORY_USER = "artifactory_user"
private const val ARTIFACTORY_PASSWORD = "artifactory_password"

class PublishingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.apply(plugin = "maven-publish")
        project.apply(plugin = "com.jfrog.artifactory")
        if (project.parent == null) {
            project.applyRootArtifactoryConfig()
        }
    }

    private fun Project.applyRootArtifactoryConfig() {
        if (!project.hasProperty(ARTIFACTORY_CONTEXT_URL)) ext[ARTIFACTORY_CONTEXT_URL] = "http://localhost"
        if (!project.hasProperty(ARTIFACTORY_USER)) ext[ARTIFACTORY_USER] = "guest"
        if (!project.hasProperty(ARTIFACTORY_PASSWORD)) ext[ARTIFACTORY_PASSWORD] = ""

        configure<ArtifactoryPluginConvention> {
            setContextUrl("${project.property(ARTIFACTORY_CONTEXT_URL)}")
            publish {
                repository {
                    setRepoKey(when {
                        "${project.version}".contains("SNAPSHOT") -> "libs-snapshot-local"
                        else -> "libs-release-local"
                    })
                    setUsername("${project.property(ARTIFACTORY_USER)}")
                    setPassword("${project.property(ARTIFACTORY_PASSWORD)}")
                    setMavenCompatible(true)
                    setPublishIvy(false)
                }
                defaults {
                    setPublishIvy(false)
                    publications("maven")
                }
            }
        }
        tasks.named<ArtifactoryTask>("artifactoryPublish") {
            isSkip = true
        }
    }
}
