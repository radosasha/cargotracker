package com.shiplocate.data.datasource.remote

import com.shiplocate.data.network.api.AuthApi
import com.shiplocate.data.network.dto.auth.LogoutErrorResponseDto
import com.shiplocate.data.network.dto.auth.SmsRequestErrorResponseDto
import com.shiplocate.data.network.dto.auth.SmsRequestRateLimitErrorResponseDto
import com.shiplocate.data.network.dto.auth.SmsVerifyErrorResponseDto
import com.shiplocate.data.network.mapper.toAuthError
import com.shiplocate.data.network.mapper.toDomain
import com.shiplocate.data.network.mapper.toDto
import com.shiplocate.data.network.mapper.toSmsRequestError
import com.shiplocate.domain.model.auth.AuthError
import com.shiplocate.domain.model.auth.AuthToken
import com.shiplocate.domain.model.auth.SmsCodeRequest
import com.shiplocate.domain.model.auth.SmsCodeResponse
import com.shiplocate.domain.model.auth.SmsCodeVerify
import com.shiplocate.domain.model.auth.SmsRequestError
import com.shiplocate.domain.model.auth.SmsVerificationError
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode

/**
 * Remote data source for authentication API calls
 */
class AuthRemoteDataSource(
    private val authApi: AuthApi,
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
        } catch (e: ResponseException) {
            println("🌐 AuthRemoteDataSource: ❌ Client error: ${e.response.status}")
            val statusCode = e.response.status.value
            val error = when (statusCode) {
                400, 500, 503 -> {
                    val errorDto: SmsRequestErrorResponseDto = e.response.body()
                    errorDto.toSmsRequestError()
                }

                429 -> {
                    val errorDto: SmsRequestRateLimitErrorResponseDto = e.response.body()
                    errorDto.toSmsRequestError()
                }

                else -> {
                    SmsRequestError.SmsServiceError(
                        message = "Request failed with status code: $statusCode",
                    )
                }
            }
            Result.failure(error)
        } catch (e: Exception) {
            println("🌐 AuthRemoteDataSource: ❌ Network error: ${e.message}")
            Result.failure(
                SmsRequestError.SmsServiceError(
                    message = e.message ?: "Network error occurred",
                ),
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
        } catch (e: ResponseException) {
            println("🌐 AuthRemoteDataSource: ❌ Verification error: ${e.response.status}")
            suspend fun parseErrorDto(): SmsVerifyErrorResponseDto? {
                return try {
                    e.response.body()
                } catch (parseError: Exception) {
                    println("🌐 AuthRemoteDataSource: Failed to parse verify error response: ${parseError.message}")
                    null
                }
            }

            val statusCode = e.response.status.value
            val error = when (statusCode) {
                400 -> {
                    val errorDto = parseErrorDto()
                    when (errorDto?.error) {
                        SmsVerificationError.VALIDATION_ERROR ->
                            SmsVerificationError.ValidationError(message = errorDto.message)

                        SmsVerificationError.CODE_ALREADY_USED ->
                            SmsVerificationError.CodeAlreadyUsed(message = errorDto.message)

                        SmsVerificationError.INVALID_PHONE ->
                            SmsVerificationError.InvalidPhone(
                                message = errorDto.message,
                                twilioErrorCode = errorDto.twilioErrorCode,
                            )

                        else ->
                            SmsVerificationError.ValidationError(
                                message = errorDto?.message ?: "Invalid request data",
                            )
                    }
                }

                401 -> {
                    val errorDto = parseErrorDto()
                    when (errorDto?.error) {
                        SmsVerificationError.CODE_INVALID ->
                            SmsVerificationError.CodeInvalid(
                                message = errorDto.message,
                                remainingAttempts = errorDto.remainingAttempts,
                            )

                        SmsVerificationError.VERIFICATION_FAILED ->
                            SmsVerificationError.VerificationFailed(message = errorDto.message)

                        else ->
                            SmsVerificationError.CodeInvalid(
                                message = errorDto?.message ?: "Invalid verification code",
                            )
                    }
                }

                403 -> {
                    val errorDto = parseErrorDto()
                    SmsVerificationError.UserBlocked(
                        message = errorDto?.message ?: "Account is blocked",
                    )
                }

                404 -> {
                    val errorDto = parseErrorDto()
                    when (errorDto?.error) {
                        SmsVerificationError.CODE_NOT_FOUND ->
                            SmsVerificationError.CodeNotFound(message = errorDto.message)

                        SmsVerificationError.CODE_EXPIRED ->
                            SmsVerificationError.CodeExpired(message = errorDto.message)

                        else ->
                            SmsVerificationError.CodeNotFound(
                                message = errorDto?.message ?: "Verification code not found or expired",
                            )
                    }
                }

                422 -> {
                    val errorDto = parseErrorDto()
                    SmsVerificationError.TooManyAttempts(
                        message = errorDto?.message ?: "Too many failed attempts. Please request a new code.",
                    )
                }

                429 -> {
                    val errorDto = parseErrorDto()
                    SmsVerificationError.RateLimitExceeded(
                        message = errorDto?.message ?: "Too many requests. Please try again later.",
                        twilioErrorCode = errorDto?.twilioErrorCode,
                    )
                }

                500 -> {
                    val errorDto = parseErrorDto()
                    when (errorDto?.error) {
                        SmsVerificationError.VERIFICATION_ERROR ->
                            SmsVerificationError.VerificationServiceError(
                                message = errorDto.message,
                                twilioErrorCode = errorDto.twilioErrorCode,
                            )

                        SmsVerificationError.INTERNAL_ERROR ->
                            SmsVerificationError.InternalError(message = errorDto.message)

                        else ->
                            SmsVerificationError.InternalError(
                                message = errorDto?.message ?: "Internal server error",
                            )
                    }
                }

                else ->
                    SmsVerificationError.InternalError(
                        message = "Request failed with status code: $statusCode",
                    )
            }
            Result.failure(error)
        } catch (e: Exception) {
            println("🌐 AuthRemoteDataSource: ❌ Network error: ${e.message}")
            Result.failure(
                SmsVerificationError.VerificationServiceError(
                    message = e.message ?: "Network error occurred",
                ),
            )
        }
    }

    /**
     * Logout user
     */
    suspend fun logout(token: String): Result<Unit> {
        println("🌐 AuthRemoteDataSource: Logging out user")
        return try {
            authApi.logout(token)
            println("🌐 AuthRemoteDataSource: ✅ Logout successful")
            Result.success(Unit)
        } catch (e: ResponseException) {
            println("🌐 AuthRemoteDataSource: ❌ Client error: ${e.response.status}")
            val error = try {
                val errorDto: LogoutErrorResponseDto = e.response.body()
                errorDto.toAuthError()
            } catch (parseError: Exception) {
                println("🌐 AuthRemoteDataSource: Failed to parse error response: ${parseError.message}")
                val statusCode = e.response.status.value
                when (statusCode) {
                    401 -> AuthError.CodeInvalid(message = "Invalid or expired token")
                    500 -> AuthError.UnknownError(
                        code = "INTERNAL_ERROR",
                        message = "Internal server error",
                    )

                    else -> AuthError.UnknownError(
                        code = "CLIENT_ERROR",
                        message = "Request failed with status code: $statusCode",
                    )
                }
            }
            Result.failure(error)
        } catch (e: ServerResponseException) {
            println("🌐 AuthRemoteDataSource: ❌ Server error: ${e.response.status}")
            val error = when (e.response.status) {
                HttpStatusCode.ServiceUnavailable ->
                    AuthError.ServiceUnavailable(
                        message = "Service temporarily unavailable. Please try again later.",
                    )

                else ->
                    AuthError.UnknownError(
                        code = "SERVER_ERROR",
                        message = "Server error: ${e.response.status}",
                    )
            }
            Result.failure(error)
        } catch (e: Exception) {
            println("🌐 AuthRemoteDataSource: ❌ Network error: ${e.message}")
            Result.failure(
                AuthError.NetworkError(
                    message = e.message ?: "Network error occurred",
                ),
            )
        }
    }

}
