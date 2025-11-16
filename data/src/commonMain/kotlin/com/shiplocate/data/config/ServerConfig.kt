package com.shiplocate.data.config

/**
 * Конфигурация сервера Traccar
 */
object ServerConfig {
    // Базовый URL сервера (без http://)
//    const val BASE_URL = "demo.traccar.org"
//    const val BASE_URL = "192.168.31.74:8082"
    const val BASE_URL = "api.shiplocate.com"

    // Таймауты для HTTP запросов
    const val CONNECT_TIMEOUT_MS = 10_000L
    const val SOCKET_TIMEOUT_MS = 30_000L

    // Интервал отправки координат (в миллисекундах)
    const val LOCATION_SEND_INTERVAL_MS = 30_000L // 30 секунд
}
