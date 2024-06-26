package _Self

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.sshAgent
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
                authType = personalToken {
                    token = "%git.github.token.commit-status.lin-bus%"
                }
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

const val ARTIFACTORY_PARAMS = "-Partifactory_contextUrl=%artifactory.contextUrl% " +
        "-Partifactory_user=%artifactory.user% " +
        "-Partifactory_password=%artifactory.password%"

object DeployNormal : BuildType({
    id = RelativeId("DeployNormal")
    name = "DeployNormal"
    description = "Make a normal deployment of the project."

    vcs {
        root(DslContext.settingsRoot)
        branchFilter = "+:<default>"
    }

    triggers {
        vcs {
        }
    }

    dependencies {
        snapshot(Build) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }

    steps {
        configuredGradle {
            name = "Publish"
            tasks = "publishToMavenLocal artifactoryPublish"
            gradleParams = ARTIFACTORY_PARAMS
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

    features {
        sshAgent {
            teamcitySshKey = "github-deploy-lin-bus"
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
            gradleParams = ARTIFACTORY_PARAMS
        }
        configuredGradle {
            name = "Switch to next snapshot version"
            tasks = "changeReleaseToNextSnapshot"
        }
        script {
            name = "Push version commits"
            scriptContent = """
                set -e
                git push origin master --tags
            """.trimIndent()
        }
    }
})
