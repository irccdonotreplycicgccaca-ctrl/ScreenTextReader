buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // Built-in Kotlin in AGP 9 depends on an older Kotlin Gradle plugin; this raises it to the catalog version.
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.spotless)
}

spotless {
    val editorConfig = rootProject.file(".editorconfig")
    // Spotless presets its own ktlint code style and ignores ktlint_code_style from .editorconfig.
    val codeStyle = mapOf("ktlint_code_style" to "android_studio")
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
            .setEditorConfigPath(editorConfig)
            .editorConfigOverride(codeStyle)
    }
    kotlinGradle {
        target("**/*.kts")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
            .setEditorConfigPath(editorConfig)
            .editorConfigOverride(codeStyle)
    }
}
