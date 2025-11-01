package com.shiplocate.core.logging

/**
 * Обработчик крешей приложения
 */
expect class CrashHandler{
    /**
     * Обрабатывает необработанное исключение
     */
    fun handleUncaughtException(
        threadName: String,
        threadId: Long,
        throwable: Throwable,
    )

    fun register()
}

