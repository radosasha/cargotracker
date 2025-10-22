package com.shiplocate.core.logging

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Модель записи лога
 */
data class LogEntry(
    val timestamp: Instant = Clock.System.now(),
    val level: LogLevel,
    val category: LogCategory,
    val message: String,
    val throwable: Throwable? = null,
    val metadata: Map<String, Any> = emptyMap(),
) {
    /**
     * Форматированное время для отображения
     */
    val formattedTime: String
        get() = timestamp.toString()

    /**
     * Полное сообщение с метаданными
     */
    val fullMessage: String
        get() =
            buildString {
                append(message)
                if (metadata.isNotEmpty()) {
                    append(" | Metadata: ")
                    append(metadata.entries.joinToString(", ") { "${it.key}=${it.value}" })
                }
                if (throwable != null) {
                    append(" | Exception: ${throwable.message}")
                }
            }
}
