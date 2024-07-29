plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation("gradle.plugin.org.cadixdev.gradle:licenser:0.6.1")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.33.1")
    val b2Version = "6.2.1"
    implementation("com.backblaze.b2:b2-sdk-core:$b2Version")
    implementation("com.backblaze.b2:b2-sdk-httpclient:$b2Version")
}

configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

gradlePlugin {
    plugins {
        create("jvm") {
            id = "org.enginehub.lin-bus.jvm"
            implementationClass = "org.enginehub.gradle.JvmPlugin"
        }
        create("license") {
            id = "org.enginehub.lin-bus.license"
            implementationClass = "org.enginehub.gradle.LicensePlugin"
        }
        create("publishing") {
            id = "org.enginehub.lin-bus.publishing"
            implementationClass = "org.enginehub.gradle.PublishingPlugin"
        }
        create("release") {
            id = "org.enginehub.lin-bus.release"
            implementationClass = "org.enginehub.gradle.ReleasePlugin"
        }
    }
}
