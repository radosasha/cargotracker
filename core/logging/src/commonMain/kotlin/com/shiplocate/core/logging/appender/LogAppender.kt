package com.shiplocate.core.logging.appender

import com.shiplocate.core.logging.LogEntry

/**
 * Базовый интерфейс для записи логов
 */
interface LogAppender {
    suspend fun append(entry: LogEntry)
}
