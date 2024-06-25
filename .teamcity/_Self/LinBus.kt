package _Self

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildSteps.GradleBuildStep
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs

fun BuildSteps.configuredGradle(init: GradleBuildStep.() -> Unit) {
    gradle {
        enableStacktrace = true

        init()
    }
}

object Build : BuildType({
    id = RelativeId("Build")
    name = "Build"

    vcs {
        root(DslContext.settingsRoot)
    }

    triggers {
        vcs {
        }
    }

    features {
        commitStatusPublisher {
            vcsRootExtId = "${DslContext.settingsRoot.id}"
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = vcsRoot()
            }
        }
    }

    steps {
        configuredGradle {
            name = "Build"
            tasks = "clean build"
        }
    }
})

object Release : BuildType({
    id = RelativeId("Release")
    name = "Release"
    description = "Make a release of the project."

    vcs {
        root(DslContext.settingsRoot)
        branchFilter = "+:<default>"
        checkoutMode = CheckoutMode.ON_AGENT
    }

    requirements {
        startsWith("teamcity.agent.name", "hetzner-")
    }

    dependencies {
        snapshot(Build) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }

    steps {
        configuredGradle {
            name = "Switch to release version"
            tasks = "changeSnapshotToRelease"
        }
        configuredGradle {
            name = "Publish release version"
            tasks = "publishToMavenLocal artifactoryPublish"
        }
        script {
            name = "Push release version commit"
            scriptContent = """
                set -e
                git push master --tags
            """.trimIndent()
        }
        configuredGradle {
            name = "Switch to next snapshot version"
            tasks = "changeReleaseToNextSnapshot"
        }
        script {
            name = "Push snapshot version commit"
            scriptContent = """
                set -e
                git push
            """.trimIndent()
        }
    }
})
