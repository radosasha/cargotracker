package com.shiplocate.core.network

/**
 * Конфигурация таймаутов для HTTP клиента (Google standards)
 */
object HttpTimeoutConfig {
    /**
     * Таймаут подключения (Connection Timeout)
     * Время на установку TCP соединения
     * Google standard: 15 секунд
     */
    const val CONNECT_TIMEOUT_MS = 15_000L

    /**
     * Таймаут чтения (Read/Socket Timeout)
     * Время ожидания данных после установки соединения
     * Google standard: 45 секунд
     */
    const val READ_TIMEOUT_MS = 45_000L

    /**
     * Таймаут записи (Write Timeout)
     * Время на отправку данных
     * Google standard: 45 секунд
     */
    const val WRITE_TIMEOUT_MS = 45_000L
}

