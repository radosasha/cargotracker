package com.shiplocate.core.logging

/**
 * Основной интерфейс логгера
 */
interface Logger {
    /**
     * Логирование с указанием уровня и категории
     */
    fun log(
        level: LogLevel,
        category: LogCategory,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, Any> = emptyMap(),
    )

    /**
     * Удобные методы для каждого уровня
     */
    fun trace(
        category: LogCategory,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.TRACE, category, message, throwable, metadata)
    }

    fun debug(
        category: LogCategory,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.DEBUG, category, message, throwable, metadata)
    }

    fun info(
        category: LogCategory,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.INFO, category, message, throwable, metadata)
    }

    fun warn(
        category: LogCategory,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.WARN, category, message, throwable, metadata)
    }

    fun error(
        category: LogCategory,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.ERROR, category, message, throwable, metadata)
    }

    fun fatal(
        category: LogCategory,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.FATAL, category, message, throwable, metadata)
    }

    /**
     * Логирование пользовательских взаимодействий
     */
    fun logUserInteraction(
        action: String,
        element: String,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.INFO, LogCategory.UI, "User interaction: $action on $element", metadata = metadata)
    }

    /**
     * Логирование сетевых запросов
     */
    fun logNetworkRequest(
        method: String,
        url: String,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.DEBUG, LogCategory.NETWORK, "Request: $method $url", metadata = metadata)
    }

    fun logNetworkResponse(
        statusCode: Int,
        url: String,
        responseTime: Long,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.DEBUG, LogCategory.NETWORK, "Response: $statusCode for $url (${responseTime}ms)", metadata = metadata)
    }

    /**
     * Логирование ошибок базы данных
     */
    fun logDatabaseQuery(
        query: String,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.DEBUG, LogCategory.DATABASE, "Query: $query", metadata = metadata)
    }

    fun logDatabaseError(
        operation: String,
        throwable: Throwable,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.ERROR, LogCategory.DATABASE, "Database error during $operation", throwable, metadata)
    }

    /**
     * Логирование аутентификации
     */
    fun logAuthEvent(
        event: String,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.INFO, LogCategory.AUTH, "Auth event: $event", metadata = metadata)
    }

    /**
     * Логирование геолокации
     */
    fun logLocationEvent(
        event: String,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.DEBUG, LogCategory.LOCATION, "Location event: $event", metadata = metadata)
    }

    /**
     * Логирование креша
     */
    fun logCrash(
        throwable: Throwable,
        metadata: Map<String, Any> = emptyMap(),
    ) {
        log(LogLevel.FATAL, LogCategory.ERROR, "Application crash", throwable, metadata)
    }
}
