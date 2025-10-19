package com.shiplocate.data.network.api

import com.shiplocate.core.network.bodyOrThrow
import com.shiplocate.data.network.dto.firebase.FirebaseTokenRequestDto
import com.shiplocate.data.network.dto.firebase.FirebaseTokenResponseDto
import com.shiplocate.data.network.dto.firebase.FirebaseTokenStatusDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

interface FirebaseTokenApi {
    suspend fun updateFirebaseToken(
        token: String,
        request: FirebaseTokenRequestDto,
    ): FirebaseTokenResponseDto

    suspend fun getFirebaseToken(token: String): FirebaseTokenStatusDto

    suspend fun clearFirebaseToken(token: String): FirebaseTokenResponseDto
}

class FirebaseTokenApiImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : FirebaseTokenApi {
    override suspend fun updateFirebaseToken(
        token: String,
        request: FirebaseTokenRequestDto,
    ): FirebaseTokenResponseDto {
        return httpClient.post("$baseUrl/api/mobile/firebase/token") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(request)
        }.bodyOrThrow()
    }

    override suspend fun getFirebaseToken(token: String): FirebaseTokenStatusDto {
        return httpClient.get("$baseUrl/api/mobile/firebase/token") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyOrThrow()
    }

    override suspend fun clearFirebaseToken(token: String): FirebaseTokenResponseDto {
        return httpClient.delete("$baseUrl/api/mobile/firebase/token") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyOrThrow()
    }
}
