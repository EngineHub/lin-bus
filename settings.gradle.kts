pluginManagement {
    repositories {
        maven {
            name = "EngineHub"
            url = uri("https://repo.enginehub.org/libs-release/")
        }
        maven {
            name = "EngineHub Central Mirror"
            url = uri("https://repo.enginehub.org/internal/maven-central-proxy/")
        }
        maven {
            name = "EngineHub Plugin Portal Mirror"
            url = uri("https://repo.enginehub.org/internal/plugin-portal-proxy/")
        }
    }
}
plugins {
    id("org.enginehub.crankcase.repo-reconfiguration") version "0.1.0"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositories {
        maven {
            name = "EngineHub"
            url = uri("https://repo.enginehub.org/libs-release/")
        }
        mavenCentral()
    }
}

includeBuild("./build-logic")

rootProject.name = "lin-bus"
include("bom")
include("common")
include("stream")
include("tree")
include("format-snbt")
include("gui")
