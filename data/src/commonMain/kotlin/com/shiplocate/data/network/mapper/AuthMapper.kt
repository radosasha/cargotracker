package com.shiplocate.data.network.mapper

import com.shiplocate.data.network.dto.auth.AuthResponseDto
import com.shiplocate.data.network.dto.auth.ErrorResponseDto
import com.shiplocate.data.network.dto.auth.MobileUserDto
import com.shiplocate.data.network.dto.auth.SmsRequestDto
import com.shiplocate.data.network.dto.auth.SmsRequestResponseDto
import com.shiplocate.data.network.dto.auth.SmsVerifyDto
import com.shiplocate.domain.model.auth.AuthError
import com.shiplocate.domain.model.auth.AuthToken
import com.shiplocate.domain.model.auth.AuthUser
import com.shiplocate.domain.model.auth.SmsCodeRequest
import com.shiplocate.domain.model.auth.SmsCodeResponse
import com.shiplocate.domain.model.auth.SmsCodeVerify
import kotlinx.datetime.Clock

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

fun ErrorResponseDto.toAuthError(): AuthError {
    val now = Clock.System.now().toEpochMilliseconds()
    return when (error) {
        "VALIDATION_ERROR" ->
            AuthError.ValidationError(
                message = message,
                timestamp = timestamp ?: now,
            )
        "RATE_LIMIT_EXCEEDED" ->
            AuthError.RateLimitError(
                message = message,
                retryAfterSeconds = retryAfterSeconds ?: 0,
                nextRetryAt = nextRetryAt,
                timestamp = timestamp ?: now,
            )
        "CODE_INVALID" ->
            AuthError.CodeInvalid(
                message = message,
                remainingAttempts = remainingAttempts,
                timestamp = timestamp ?: now,
            )
        "CODE_EXPIRED" ->
            AuthError.CodeExpired(
                message = message,
                timestamp = timestamp ?: now,
            )
        "CODE_NOT_FOUND" ->
            AuthError.CodeNotFound(
                message = message,
                timestamp = timestamp ?: now,
            )
        "TOO_MANY_ATTEMPTS" ->
            AuthError.TooManyAttempts(
                message = message,
                timestamp = timestamp ?: now,
            )
        "CODE_ALREADY_USED" ->
            AuthError.CodeAlreadyUsed(
                message = message,
                timestamp = timestamp ?: now,
            )
        "USER_BLOCKED" ->
            AuthError.UserBlocked(
                message = message,
                timestamp = timestamp ?: now,
            )
        else ->
            AuthError.UnknownError(
                code = error,
                message = message,
                timestamp = timestamp ?: now,
            )
    }
}
