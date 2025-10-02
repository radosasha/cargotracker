package com.tracker.data.network.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

/**
 * Android реализация провайдера HTTP клиента
 */
actual class HttpClientProvider {
    actual fun createHttpClient(): HttpClient {
        return createHttpClient(Android.create())
    }
}
