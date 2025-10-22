package com.shiplocate.core.logging.appender

import com.shiplocate.core.logging.LogEntry
import com.shiplocate.core.logging.LogLevel

/**
 * Appender для вывода логов в консоль
 */
class ConsoleAppender : LogAppender {
    override suspend fun append(entry: LogEntry) {
        val formattedMessage = formatLogEntry(entry)

        // В зависимости от уровня используем разные цвета (если поддерживается)
        when (entry.level) {
            LogLevel.ERROR, LogLevel.FATAL -> println("🔴 $formattedMessage")
            LogLevel.WARN -> println("🟡 $formattedMessage")
            LogLevel.INFO -> println("🔵 $formattedMessage")
            LogLevel.DEBUG -> println("🟢 $formattedMessage")
            LogLevel.TRACE -> println("⚪ $formattedMessage")
        }
    }

    private fun formatLogEntry(entry: LogEntry): String {
        return buildString {
            append("[${entry.formattedTime}] ")
            append("[${entry.level.name}] ")
            append("[${entry.category.displayName}] ")
            append(entry.fullMessage)

            // Добавляем стектрейс если есть исключение
            entry.throwable?.let { throwable ->
                append("\nStack trace:")
                append("\n${throwable.stackTraceToString()}")
            }
        }
    }
}
