package com.shiplocate.data.datasource.remote

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
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
    private val logger: Logger,
) {
    /**
     * Request SMS verification code
     */
    suspend fun requestSmsCode(request: SmsCodeRequest): Result<SmsCodeResponse> {
        logger.info(LogCategory.AUTH, "AuthRemoteDataSource: Requesting SMS code for ${request.phone}")
        return try {
            val response = authApi.requestSmsCode(request.toDto())
            logger.info(LogCategory.AUTH, "AuthRemoteDataSource: SMS request successful")
            Result.success(response.toDomain())
        } catch (e: ResponseException) {
            logger.warn(LogCategory.AUTH, "AuthRemoteDataSource: Client error ${e.response.status}", e)
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
            logger.error(LogCategory.AUTH, "AuthRemoteDataSource: Network error requesting SMS code", e)
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
        logger.info(LogCategory.AUTH, "AuthRemoteDataSource: Verifying SMS code for ${verify.phone}")
        return try {
            val response = authApi.verifySmsCode(verify.toDto())
            logger.info(LogCategory.AUTH, "AuthRemoteDataSource: SMS verification successful")
            Result.success(response.toDomain())
        } catch (e: ResponseException) {
            logger.warn(LogCategory.AUTH, "AuthRemoteDataSource: Verification error ${e.response.status}", e)
            suspend fun parseErrorDto(): SmsVerifyErrorResponseDto? {
                return try {
                    e.response.body()
                } catch (parseError: Exception) {
                    logger.error(
                        LogCategory.AUTH,
                        "AuthRemoteDataSource: Failed to parse verify error response",
                        parseError,
                    )
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
            logger.error(LogCategory.AUTH, "AuthRemoteDataSource: Network error verifying SMS code", e)
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
        logger.info(LogCategory.AUTH, "AuthRemoteDataSource: Logging out user")
        return try {
            authApi.logout(token)
            logger.info(LogCategory.AUTH, "AuthRemoteDataSource: Logout successful")
            Result.success(Unit)
        } catch (e: ResponseException) {
            logger.warn(LogCategory.AUTH, "AuthRemoteDataSource: Logout client error ${e.response.status}", e)
            val error = try {
                val errorDto: LogoutErrorResponseDto = e.response.body()
                errorDto.toAuthError()
            } catch (parseError: Exception) {
                logger.error(LogCategory.AUTH, "AuthRemoteDataSource: Failed to parse logout error", parseError)
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
            logger.error(LogCategory.AUTH, "AuthRemoteDataSource: Server error ${e.response.status}", e)
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
            logger.error(LogCategory.AUTH, "AuthRemoteDataSource: Network error logging out", e)
            Result.failure(
                AuthError.NetworkError(
                    message = e.message ?: "Network error occurred",
                ),
            )
        }
    }

}
