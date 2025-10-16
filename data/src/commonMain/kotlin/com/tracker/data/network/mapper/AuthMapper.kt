package com.tracker.data.network.mapper

import com.tracker.data.network.dto.auth.AuthResponseDto
import com.tracker.data.network.dto.auth.ErrorResponseDto
import com.tracker.data.network.dto.auth.MobileUserDto
import com.tracker.data.network.dto.auth.SmsRequestDto
import com.tracker.data.network.dto.auth.SmsRequestResponseDto
import com.tracker.data.network.dto.auth.SmsVerifyDto
import com.tracker.domain.model.auth.AuthError
import com.tracker.domain.model.auth.AuthToken
import com.tracker.domain.model.auth.AuthUser
import com.tracker.domain.model.auth.SmsCodeRequest
import com.tracker.domain.model.auth.SmsCodeResponse
import com.tracker.domain.model.auth.SmsCodeVerify
import kotlinx.datetime.Clock

/**
 * Mappers for Auth DTOs <-> Domain models
 */

// Domain -> DTO
fun SmsCodeRequest.toDto() = SmsRequestDto(phone = phone)

fun SmsCodeVerify.toDto() =
    SmsVerifyDto(
        phone = phone,
        code = code,
        deviceInfo = deviceInfo,
    )

// DTO -> Domain
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

// Error mapping
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
