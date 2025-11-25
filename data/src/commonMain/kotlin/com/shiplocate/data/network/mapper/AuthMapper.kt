package com.shiplocate.data.network.mapper

import com.shiplocate.data.network.dto.auth.AuthResponseDto
import com.shiplocate.data.network.dto.auth.LogoutErrorResponseDto
import com.shiplocate.data.network.dto.auth.MobileUserDto
import com.shiplocate.data.network.dto.auth.SmsRequestDto
import com.shiplocate.data.network.dto.auth.SmsRequestErrorResponseDto
import com.shiplocate.data.network.dto.auth.SmsRequestRateLimitErrorResponseDto
import com.shiplocate.data.network.dto.auth.SmsRequestResponseDto
import com.shiplocate.data.network.dto.auth.SmsVerifyDto
import com.shiplocate.domain.model.auth.AuthError
import com.shiplocate.domain.model.auth.AuthToken
import com.shiplocate.domain.model.auth.AuthUser
import com.shiplocate.domain.model.auth.SmsCodeRequest
import com.shiplocate.domain.model.auth.SmsCodeResponse
import com.shiplocate.domain.model.auth.SmsCodeVerify
import com.shiplocate.domain.model.auth.SmsRequestError

/**
 * Mappers for Auth DTOs <-> Domain models
 */

fun SmsCodeRequest.toDto() = SmsRequestDto(phone = phone)

fun SmsCodeVerify.toDto() =
    SmsVerifyDto(
        phone = phone,
        code = code,
        deviceInfo = deviceInfo,
    )

fun SmsRequestResponseDto.toDomain() =
    SmsCodeResponse(
        status = status,
        message = message,
    )

fun AuthResponseDto.toDomain() =
    AuthToken(
        token = token,
        user = user.toDomain(),
    )

fun MobileUserDto.toDomain() =
    AuthUser(
        id = id,
        phone = phone,
        name = name,
    )

/**
 * Converts SmsRequestErrorResponseDto to SmsRequestError
 */
fun SmsRequestErrorResponseDto.toSmsRequestError(): SmsRequestError {
    return when (error) {
        SmsRequestError.VALIDATION_ERROR -> SmsRequestError.ValidationError(message = message)
        SmsRequestError.SMS_SERVICE_ERROR -> SmsRequestError.SmsServiceError(message = message)
        else -> SmsRequestError.SmsServiceError(
            message = "Unknown error: $error - $message",
        )
    }
}

/**
 * Converts SmsRequestRateLimitErrorResponseDto to SmsRequestError
 */
fun SmsRequestRateLimitErrorResponseDto.toSmsRequestError(): SmsRequestError {
    return SmsRequestError.RateLimitExceeded(
        message = message,
        retryAfterSeconds = retryAfterSeconds ?: 10,
        nextRetryAt = nextRetryAt,
    )
}

/**
 * Converts LogoutErrorResponseDto to AuthError
 */
fun LogoutErrorResponseDto.toAuthError(): AuthError {
    return when (error) {
        "INVALID_REQUEST" -> AuthError.ValidationError(message = message)
        "INVALID_TOKEN" -> AuthError.CodeInvalid(message = message)
        "DATABASE_ERROR" -> AuthError.UnknownError(
            code = "DATABASE_ERROR",
            message = message,
        )
        "INTERNAL_ERROR" -> AuthError.UnknownError(
            code = "INTERNAL_ERROR",
            message = message,
        )
        "SUCCESS" -> AuthError.UnknownError(
            code = "SUCCESS",
            message = message,
        )
        else -> AuthError.UnknownError(
            code = error,
            message = message,
        )
    }
}
