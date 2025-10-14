package com.tracker.data.network.api

import com.tracker.core.network.bodyOrThrow
import com.tracker.data.network.dto.auth.AuthResponseDto
import com.tracker.data.network.dto.auth.SmsRequestDto
import com.tracker.data.network.dto.auth.SmsRequestResponseDto
import com.tracker.data.network.dto.auth.SmsVerifyDto
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface AuthApi {
    suspend fun requestSmsCode(request: SmsRequestDto): SmsRequestResponseDto
    suspend fun verifySmsCode(verify: SmsVerifyDto): AuthResponseDto
}

class AuthApiImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : AuthApi {
    
    override suspend fun requestSmsCode(request: SmsRequestDto): SmsRequestResponseDto {
        return httpClient.post("$baseUrl/api/mobile/auth/request") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.bodyOrThrow()
    }
    
    override suspend fun verifySmsCode(verify: SmsVerifyDto): AuthResponseDto {
        return httpClient.post("$baseUrl/api/mobile/auth/verify") {
            contentType(ContentType.Application.Json)
            setBody(verify)
        }.bodyOrThrow()
    }
}
