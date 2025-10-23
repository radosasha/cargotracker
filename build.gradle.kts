// Плагины, которые используются в buildSrc Convention Plugins, не нужно объявлять здесь
// Они уже загружены в buildSrc/build.gradle.kts
plugins {
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
    
    // Add the dependency for the Google services Gradle plugin
    alias(libs.plugins.google.services) apply false
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
            // Комплексный подход - исключаем ВСЕ директории со сгенерированными файлами
            exclude("**/build/**")
            exclude("**/generated/**")
            exclude("**/ksp/**")
            exclude("**/tmp/**")
            exclude("**/intermediates/**")
            // Исключаем iOS-специфичные файлы с платформо-зависимым кодом
            exclude("**/DatabaseProvider.ios.kt")
            include("**/kotlin/**")
        }

        // Отключить проверку для конкретных source sets
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }

        // Конфигурация правил в ktlint.yml файле

    }
}
