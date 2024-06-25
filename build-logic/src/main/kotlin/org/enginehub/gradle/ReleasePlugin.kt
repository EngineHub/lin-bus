package org.enginehub.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

const val RELEASE_GROUP = "Release Tasks"

class ReleasePlugin @Inject constructor(
    private val execOperations: ExecOperations
) : Plugin<Project> {
    override fun apply(project: Project) {
        val versionFile = project.file("version.txt")
        if (!versionFile.exists()) {
            throw IllegalStateException("version.txt file does not exist")
        }

        project.version = getVersion(versionFile)

        project.tasks.register("changeSnapshotToRelease") {
            group = RELEASE_GROUP
            description = "Change the version from a snapshot to a release version, commit it, and tag it"
            doLast {
                val version = getVersion(versionFile)
                val newVersion = version.removeSuffix("-SNAPSHOT")
                if (version == newVersion) {
                    throw IllegalStateException("Version is not a snapshot version")
                }
                replaceVersion(versionFile, newVersion)

                execOperations.exec {
                    commandLine("git", "commit", "-m", "Release version $newVersion", versionFile.absolutePath)
                }
                execOperations.exec {
                    commandLine("git", "tag", "v${newVersion}", "-m", "Release version $newVersion")
                }
            }
        }
        project.tasks.register("changeReleaseToNextSnapshot") {
            group = RELEASE_GROUP
            description = "Change the version from a release to the next snapshot version and commit it"
            doLast {
                val version = getVersion(versionFile)
                if (version.endsWith("-SNAPSHOT")) {
                    throw IllegalStateException("Version is already a snapshot version")
                }
                val parts = version.split(".")
                val lastPart = parts.last()
                val newLastPart = (lastPart.toInt() + 1).toString()
                val newVersion = parts.dropLast(1).joinToString(".") + ".$newLastPart-SNAPSHOT"
                replaceVersion(versionFile, newVersion)

                execOperations.exec {
                    commandLine(
                        "git",
                        "commit",
                        "-m",
                        "Switch to next snapshot version $newVersion",
                        versionFile.absolutePath
                    )
                }
            }
        }
    }

    private fun getVersion(versionFile: File): String = versionFile.readText().trim()

    private fun replaceVersion(versionFile: File, version: String) = versionFile.writeText(version)
}
