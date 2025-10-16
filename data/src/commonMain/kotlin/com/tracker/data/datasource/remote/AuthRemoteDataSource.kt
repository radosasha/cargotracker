package com.tracker.data.datasource.remote

import com.tracker.data.network.api.AuthApi
import com.tracker.data.network.dto.auth.ErrorResponseDto
import com.tracker.data.network.mapper.toAuthError
import com.tracker.data.network.mapper.toDomain
import com.tracker.data.network.mapper.toDto
import com.tracker.domain.model.auth.AuthError
import com.tracker.domain.model.auth.AuthToken
import com.tracker.domain.model.auth.SmsCodeRequest
import com.tracker.domain.model.auth.SmsCodeResponse
import com.tracker.domain.model.auth.SmsCodeVerify
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

/**
 * Remote data source for authentication API calls
 */
class AuthRemoteDataSource(
    private val authApi: AuthApi,
    private val json: Json
) {
    
    /**
     * Request SMS verification code
     */
    suspend fun requestSmsCode(request: SmsCodeRequest): Result<SmsCodeResponse> {
        println("🌐 AuthRemoteDataSource: Requesting SMS code for ${request.phone}")
        return try {
            val response = authApi.requestSmsCode(request.toDto())
            println("🌐 AuthRemoteDataSource: ✅ SMS request successful")
            Result.success(response.toDomain())
        } catch (e: ClientRequestException) {
            println("🌐 AuthRemoteDataSource: ❌ Client error: ${e.response.status}")
            Result.failure(parseClientError(e))
        } catch (e: ServerResponseException) {
            println("🌐 AuthRemoteDataSource: ❌ Server error: ${e.response.status}")
            val error = when (e.response.status) {
                HttpStatusCode.ServiceUnavailable -> AuthError.ServiceUnavailable(
                    message = "Service temporarily unavailable. Please try again later."
                )
                else -> AuthError.UnknownError(
                    code = "SERVER_ERROR",
                    message = "Server error: ${e.response.status}"
                )
            }
            Result.failure(error)
        } catch (e: Exception) {
            println("🌐 AuthRemoteDataSource: ❌ Network error: ${e.message}")
            Result.failure(
                AuthError.NetworkError(
                    message = e.message ?: "Network error occurred"
                )
            )
        }
    }
    
    /**
     * Verify SMS code and authenticate
     */
    suspend fun verifySmsCode(verify: SmsCodeVerify): Result<AuthToken> {
        println("🌐 AuthRemoteDataSource: Verifying SMS code for ${verify.phone}")
        return try {
            val response = authApi.verifySmsCode(verify.toDto())
            println("🌐 AuthRemoteDataSource: ✅ SMS verification successful")
            Result.success(response.toDomain())
        } catch (e: ClientRequestException) {
            println("🌐 AuthRemoteDataSource: ❌ Client error: ${e.response.status}")
            Result.failure(parseClientError(e))
        } catch (e: ServerResponseException) {
            println("🌐 AuthRemoteDataSource: ❌ Server error: ${e.response.status}")
            val error = when (e.response.status) {
                HttpStatusCode.ServiceUnavailable -> AuthError.ServiceUnavailable(
                    message = "Service temporarily unavailable. Please try again later."
                )
                else -> AuthError.UnknownError(
                    code = "SERVER_ERROR",
                    message = "Server error: ${e.response.status}"
                )
            }
            Result.failure(error)
        } catch (e: Exception) {
            println("🌐 AuthRemoteDataSource: ❌ Network error: ${e.message}")
            Result.failure(
                AuthError.NetworkError(
                    message = e.message ?: "Network error occurred"
                )
            )
        }
    }
    
    /**
     * Parse client error (4xx) from API response
     */
    private suspend fun parseClientError(e: ClientRequestException): AuthError {
        return try {
            val errorBody = e.response.body<String>()
            val errorDto = json.decodeFromString<ErrorResponseDto>(errorBody)
            errorDto.toAuthError()
        } catch (parseError: Exception) {
            // Fallback if we can't parse the error response
            when (e.response.status) {
                HttpStatusCode.BadRequest -> AuthError.ValidationError(
                    message = "Invalid request data"
                )
                HttpStatusCode.Unauthorized -> AuthError.CodeInvalid(
                    message = "Invalid verification code"
                )
                HttpStatusCode.NotFound -> AuthError.CodeNotFound(
                    message = "Verification code not found or expired"
                )
                HttpStatusCode.Forbidden -> AuthError.UserBlocked(
                    message = "Account is blocked"
                )
                HttpStatusCode.TooManyRequests -> AuthError.RateLimitError(
                    message = "Too many requests. Please try again later.",
                    retryAfterSeconds = 60
                )
                HttpStatusCode.ServiceUnavailable -> AuthError.ServiceUnavailable(
                    message = "Service temporarily unavailable. Please try again later."
                )
                else -> AuthError.UnknownError(
                    code = "CLIENT_ERROR",
                    message = "Request failed: ${e.response.status}"
                )
            }
        }
    }
}

