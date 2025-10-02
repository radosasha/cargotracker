package com.tracker.data.config

/**
 * Конфигурация сервера Traccar
 */
object ServerConfig {
    const val SERVER_URL = "http://demo.traccar.org:5055"
    const val DEVICE_ID = "40329715"
    
    // Таймауты для HTTP запросов
    const val CONNECT_TIMEOUT_MS = 10_000L
    const val SOCKET_TIMEOUT_MS = 30_000L
    
    // Интервал отправки координат (в миллисекундах)
    const val LOCATION_SEND_INTERVAL_MS = 30_000L // 30 секунд
}
