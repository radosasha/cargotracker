package com.shiplocate.core.logging

import com.shiplocate.core.logging.appender.ConsoleAppender
import com.shiplocate.core.logging.appender.FileAppender
import com.shiplocate.core.logging.appender.NetworkAppender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Реализация логгера
 */
class LoggerImpl(
    private val config: LoggingConfig,
    private val fileAppender: FileAppender,
    private val consoleAppender: ConsoleAppender,
    private val networkAppender: NetworkAppender,
) : Logger {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun log(
        level: LogLevel,
        category: LogCategory,
        message: String,
        throwable: Throwable?,
        metadata: Map<String, Any>,
    ) {
        // Проверяем, должен ли лог быть записан
        if (!shouldLog(level, category)) {
            return
        }

        val entry =
            LogEntry(
                level = level,
                category = category,
                message = message,
                throwable = throwable,
                metadata = metadata,
            )

        // Асинхронная запись логов
        scope.launch {
            if (config.enableConsoleLogging) {
                consoleAppender.append(entry)
            }

            if (config.enableFileLogging) {
                fileAppender.append(entry)
            }

            // Отправка на сервер (пока не реализовано)
            if (shouldSendToServer(level)) {
                networkAppender.append(entry)
            }
        }
    }

    /**
     * Проверяет, должен ли лог быть записан
     */
    private fun shouldLog(
        level: LogLevel,
        category: LogCategory,
    ): Boolean {
        // Проверяем, включена ли категория
        if (!config.shouldLogCategory(category)) {
            return false
        }

        // Проверяем уровень логирования
        val minLevel = config.getMinLevelForCategory(category)
        return level.shouldLog(minLevel)
    }

    /**
     * Определяет, нужно ли отправлять лог на сервер
     */
    private fun shouldSendToServer(level: LogLevel): Boolean {
        return level == LogLevel.ERROR || level == LogLevel.FATAL
    }
}
