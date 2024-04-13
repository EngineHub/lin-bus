plugins {
    id("org.enginehub.lin-bus.publishing")
    id("net.researchgate.release") version "3.0.2"
}

release {
    tagTemplate = "v\${version}"
    buildTasks = listOf<String>()
    git.requireBranch = "master"
}
