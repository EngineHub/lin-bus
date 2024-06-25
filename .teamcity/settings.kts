import _Self.Build
import _Self.DeployNormal
import _Self.Release
import jetbrains.buildServer.configs.kotlin.project
import jetbrains.buildServer.configs.kotlin.version

/*
To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2024.03"

project {
    buildType(Build)
    buildType(DeployNormal)
    buildType(Release)
}
