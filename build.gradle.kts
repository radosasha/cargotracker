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

// Общая конфигурация ktlint для всех модулей
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.2.1")
        android.set(true)
        ignoreFailures.set(false)
        enableExperimentalRules.set(false)
        
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
            include("**/kotlin/**")
        }
        
        // Отключить проблемные правила (новый синтаксис)
        disabledRules.set(listOf(
            "function-naming",
            "class-naming",
            "discouraged-comment-location",
            "no-empty-first-line-in-class-body",
            "blank-line-before-declaration",
            "wrapping",
            "parameter-list-wrapping",
            "multiline-expression-wrapping"
        ))
        
    }
}