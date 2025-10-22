package com.shiplocate.core.logging

/**
 * Builder для создания конфигурации логгера
 */
class LoggerBuilder {
    private var config = LoggingConfig()

    /**
     * Включить файловое логирование
     */
    fun enableFileLogging(): LoggerBuilder {
        config = config.copy(enableFileLogging = true)
        return this
    }

    /**
     * Включить консольное логирование
     */
    fun enableConsoleLogging(): LoggerBuilder {
        config = config.copy(enableConsoleLogging = true)
        return this
    }

    /**
     * Включить логирование сетевых запросов
     */
    fun logNetworkRequests(): LoggerBuilder {
        config = config.copy(enableNetworkLogging = true)
        return this
    }

    /**
     * Включить логирование стектрейсов
     */
    fun logStackTraces(): LoggerBuilder {
        config = config.copy(enableStackTraces = true)
        return this
    }

    /**
     * Включить логирование запросов к базе данных
     */
    fun logDatabaseQueries(): LoggerBuilder {
        config = config.copy(enableDatabaseLogging = true)
        return this
    }

    /**
     * Включить логирование пользовательских взаимодействий
     */
    fun enableUserUiInteractions(): LoggerBuilder {
        config = config.copy(enableUserInteractions = true)
        return this
    }

    /**
     * Включить логирование крешей
     */
    fun crashErrorLogging(): LoggerBuilder {
        config = config.copy(enableCrashLogging = true)
        return this
    }

    /**
     * Установить уровень логирования
     */
    fun setLogLevel(level: LogLevel): LoggerBuilder {
        config = config.copy(logLevel = level)
        return this
    }

    /**
     * Установить максимальный размер файла
     */
    fun setMaxFileSize(sizeInBytes: Long): LoggerBuilder {
        config = config.copy(maxFileSize = sizeInBytes)
        return this
    }

    /**
     * Установить максимальное количество файлов
     */
    fun setMaxFiles(maxFiles: Int): LoggerBuilder {
        config = config.copy(maxFiles = maxFiles)
        return this
    }

    /**
     * Включить ротацию файлов по часам
     */
    fun setFileHours(enabled: Boolean): LoggerBuilder {
        config = config.copy(fileHours = enabled)
        return this
    }

    /**
     * Установить директорию для логов
     */
    fun setLogDirectory(directory: String): LoggerBuilder {
        config = config.copy(logDirectory = directory)
        return this
    }

    /**
     * Установить уровень для конкретной категории
     */
    fun setCategoryLevel(
        category: LogCategory,
        level: LogLevel,
    ): LoggerBuilder {
        val newCategoryLevels = config.categoryLevels.toMutableMap()
        newCategoryLevels[category] = level
        config = config.copy(categoryLevels = newCategoryLevels)
        return this
    }

    /**
     * Создать конфигурацию
     */
    fun buildConfig(): LoggingConfig {
        return config
    }

    /**
     * Создать логгер с текущей конфигурацией
     * Внимание: Этот метод устарел. Используйте DI для создания Logger
     */
    @Deprecated("Используйте DI для создания Logger")
    fun build(): Logger {
        throw UnsupportedOperationException("Используйте DI для создания Logger. LoggerBuilder теперь используется только для создания LoggingConfig")
    }

    /**
     * Создать конфигурацию для debug сборки
     */
    fun buildDebug(): Logger {
        return LoggerBuilder()
            .enableFileLogging()
            .enableConsoleLogging()
            .logNetworkRequests()
            .logStackTraces()
            .logDatabaseQueries()
            .enableUserUiInteractions()
            .crashErrorLogging()
            .setLogLevel(LogLevel.DEBUG)
            .setFileHours(true)
            .setMaxFileSize(10 * 1024 * 1024) // 10MB
            .setMaxFiles(5)
            .build()
    }

    /**
     * Создать конфигурацию для release сборки
     */
    fun buildRelease(): Logger {
        return LoggerBuilder()
            .enableFileLogging()
            .crashErrorLogging()
            .setLogLevel(LogLevel.WARN)
            .setFileHours(true)
            .setMaxFileSize(5 * 1024 * 1024) // 5MB
            .setMaxFiles(3)
            .build()
    }
}
