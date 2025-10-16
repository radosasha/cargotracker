// Плагины, которые используются в buildSrc Convention Plugins, не нужно объявлять здесь
// Они уже загружены в buildSrc/build.gradle.kts
plugins {
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
}