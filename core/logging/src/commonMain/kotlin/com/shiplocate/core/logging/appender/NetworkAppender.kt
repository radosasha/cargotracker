package com.shiplocate.core.logging.appender

import com.shiplocate.core.logging.LogEntry

/**
 * Appender для отправки логов на сервер
 * Пока пустая реализация - будет реализована позже
 */
class NetworkAppender : LogAppender {
    override suspend fun append(entry: LogEntry) {
        // TODO: Реализовать отправку логов на сервер
        // Пока просто логируем в консоль для отладки
        println("NetworkAppender: Would send to server - ${entry.level.name} ${entry.category.displayName}: ${entry.message}")
    }

    /**
     * Отправляет лог на сервер
     */
    private suspend fun sendToServer(entry: LogEntry) {
        // TODO: Реализовать HTTP запрос на сервер логирования
        // Можно использовать Ktor client для отправки
    }

    /**
     * Отправляет батч логов на сервер
     */
    private suspend fun sendBatchToServer(entries: List<LogEntry>) {
        // TODO: Реализовать батчевую отправку для производительности
    }
}
