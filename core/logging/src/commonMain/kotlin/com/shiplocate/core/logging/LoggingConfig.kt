package com.shiplocate.core.logging

/**
 * Конфигурация системы логирования
 */
data class LoggingConfig(
    val logLevel: LogLevel = LogLevel.DEBUG,
    val enableFileLogging: Boolean = false,
    val enableConsoleLogging: Boolean = true,
    val enableNetworkLogging: Boolean = false,
    val enableStackTraces: Boolean = false,
    val enableDatabaseLogging: Boolean = false,
    val enableUserInteractions: Boolean = false,
    val enableCrashLogging: Boolean = false,
    val fileHours: Boolean = false,
    val maxFileSize: Long = 2 * 1024 * 1024, // 2MB
    val maxHoursHistory: Int = 48,
    val categoryLevels: Map<LogCategory, LogLevel> = emptyMap(),
    val logDirectory: String = "logs",
) {
    /**
     * Получить минимальный уровень для категории
     */
    fun getMinLevelForCategory(category: LogCategory): LogLevel {
        return categoryLevels[category] ?: logLevel
    }

    /**
     * Проверить, должна ли категория логироваться
     */
    fun shouldLogCategory(category: LogCategory): Boolean {
        return when (category) {
            LogCategory.NETWORK -> enableNetworkLogging
            LogCategory.DATABASE -> enableDatabaseLogging
            LogCategory.UI -> enableUserInteractions
            LogCategory.ERROR -> enableCrashLogging || enableStackTraces
            else -> true
        }
    }
}
