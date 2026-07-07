plugins {
    `kotlin-dsl`
}

configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

dependencies {
    implementation(libs.crankcase.java)
    implementation(libs.crankcase.javaLibrary)
}
