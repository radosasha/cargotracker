package com.shiplocate.core.logging

import android.content.Context

/**
 * Android-специфичная реализация обработчика крешей
 */
actual class CrashHandler(
    private val context: Context,
    private val logger: Logger,
) {
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    init {
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        // Устанавливаем наш обработчик
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleUncaughtException(thread.name, thread.id, throwable)
        }
    }

    actual fun handleUncaughtException(
        threadName: String,
        threadId: Long,
        throwable: Throwable,
    ) {
        // Логируем креш
        logger.logCrash(
            throwable,
            mapOf(
                "thread" to threadName,
                "threadId" to threadId,
                "platform" to "Android",
            ),
        )

        // Передаем управление системному обработчику
        defaultHandler?.uncaughtException(Thread.currentThread(), throwable)
    }
}
