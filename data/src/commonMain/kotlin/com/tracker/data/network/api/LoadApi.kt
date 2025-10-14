package com.tracker.data.network.api

import com.tracker.core.network.bodyOrThrow
import com.tracker.data.network.dto.load.LoadDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

/**
 * API interface for Load operations
 */
interface LoadApi {
    /**
     * Get loads for authenticated user
     * Requires Bearer token in Authorization header
     * 
     * @param token JWT token for authentication
     * @return List of LoadDto
     * @throws Exception if request fails
     */
    suspend fun getLoads(token: String): List<LoadDto>
}

/**
 * Implementation of LoadApi
 * Calls /api/mobile/loads endpoint
 */
class LoadApiImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : LoadApi {
    
    override suspend fun getLoads(token: String): List<LoadDto> {
        println("🌐 LoadApi: Getting loads from $baseUrl")
        
        val response = httpClient.get("$baseUrl/api/mobile/loads") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        println("🌐 LoadApi: Response status: ${response.status}")
        return response.bodyOrThrow()
    }
}
