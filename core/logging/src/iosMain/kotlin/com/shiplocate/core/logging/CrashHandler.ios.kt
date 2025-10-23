package com.shiplocate.core.logging

/**
 * iOS-специфичная реализация обработчика крешей
 */
actual class CrashHandler(
    private val logger: Logger,
) {

    init {
        println("iOS CrashHandler initialized")
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
                "platform" to "iOS",
            ),
        )
    }
}
