package com.tracker.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Провайдер HTTP клиента для сетевых запросов
 */
expect class HttpClientProvider() {
     fun createHttpClient(): HttpClient
}

/**
 * Создает настроенный HTTP клиент
 */
fun createHttpClient(engine: HttpClientEngine): HttpClient {
    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("🌐 HTTP: $message")
                }
            }
            level = LogLevel.ALL
        }
        
        // Don't validate 2xx responses - we handle errors manually
        expectSuccess = false
    }
}
