package com.tracker.data.config

/**
 * Конфигурация сервера Traccar
 */
object ServerConfig {
    // Базовый URL сервера
//    private const val BASE_URL = "http://demo.traccar.org"
    private const val BASE_URL = "http://192.168.31.180"

    // URL для OsmAnd протокола (порт 5055)
    const val OSMAND_SERVER_URL = "$BASE_URL:5055"
    
    // URL для Flespi протокола (порт 5149)
    const val FLESPI_SERVER_URL = "$BASE_URL:5149"
    

    // Таймауты для HTTP запросов
    const val CONNECT_TIMEOUT_MS = 10_000L
    const val SOCKET_TIMEOUT_MS = 30_000L
    
    // Интервал отправки координат (в миллисекундах)
    const val LOCATION_SEND_INTERVAL_MS = 30_000L // 30 секунд
}
