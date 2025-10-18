package com.shiplocate.data.config

/**
 * Конфигурация сервера Traccar
 */
object ServerConfig {
    // Базовый URL сервера (без http://)
//    const val BASE_URL = "demo.traccar.org"
//    const val BASE_URL = "192.168.31.116"
    const val BASE_URL = "44.216.176.38"

    // URL для OsmAnd протокола (порт 5055)
    const val OSMAND_SERVER_URL = "http://$BASE_URL:5055"

    // URL для Flespi протокола (порт 5149)
    const val FLESPI_SERVER_URL = "http://$BASE_URL:5149"

    // Таймауты для HTTP запросов
    const val CONNECT_TIMEOUT_MS = 10_000L
    const val SOCKET_TIMEOUT_MS = 30_000L

    // Интервал отправки координат (в миллисекундах)
    const val LOCATION_SEND_INTERVAL_MS = 30_000L // 30 секунд
}
