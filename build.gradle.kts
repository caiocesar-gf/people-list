
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.6")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

subprojects {
    configurations.all {
        exclude(group = "com.intellij", module = "annotations")
        resolutionStrategy.force("org.jetbrains:annotations:23.0.0")
    }
}