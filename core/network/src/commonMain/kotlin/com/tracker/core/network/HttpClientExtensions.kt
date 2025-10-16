package com.tracker.core.network

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

/**
 * Extension функция для безопасного получения body с автоматической обработкой ошибок
 * 
 * @throws ClientRequestException для 4xx ответов
 * @throws ServerResponseException для 5xx ответов
 */
suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
    if (!status.isSuccess()) {
        println("🌐 HttpResponse: Non-success status ${status.value}, throwing exception")
        when {
            status.value in 400..499 -> {
                throw ClientRequestException(this, body())
            }
            status.value in 500..599 -> {
                throw ServerResponseException(this, body())
            }
            else -> {
                throw Exception("Unexpected status code: ${status.value}")
            }
        }
    }
    
    println("🌐 HttpResponse: Success status ${status.value}, parsing body")
    return body()
}









