plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
}

configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

gradlePlugin {
    plugins {
        create("jvm") {
            id = "org.enginehub.lin-bus.jvm"
            implementationClass = "org.enginehub.gradle.JvmPlugin"
        }
    }
}
