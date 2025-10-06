package com.tracker.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

/**
 * iOS реализация провайдера HTTP клиента
 */
actual class HttpClientProvider {
    actual fun createHttpClient(): HttpClient {
        return createHttpClient(Darwin.create())
    }
}
