package com.tracker.domain.model.auth

import kotlinx.datetime.Clock

/**
 * Domain model for authentication errors
 */
sealed class AuthError(
    open val code: String,
    override val message: String,
    open val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) : Exception(message) {
    data class ValidationError(
        override val code: String = "VALIDATION_ERROR",
        override val message: String,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AuthError(code, message, timestamp)

    data class RateLimitError(
        override val code: String = "RATE_LIMIT_EXCEEDED",
        override val message: String,
        val retryAfterSeconds: Long,
        val nextRetryAt: String? = null,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AuthError(code, message, timestamp)

    data class CodeInvalid(
        override val code: String = "CODE_INVALID",
        override val message: String,
        val remainingAttempts: Int? = null,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AuthError(code, message, timestamp)

    data class CodeExpired(
        override val code: String = "CODE_EXPIRED",
        override val message: String,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AuthError(code, message, timestamp)

    data class CodeNotFound(
        override val code: String = "CODE_NOT_FOUND",
        override val message: String,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AuthError(code, message, timestamp)

    data class TooManyAttempts(
        override val code: String = "TOO_MANY_ATTEMPTS",
        override val message: String,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AuthError(code, message, timestamp)

    data class CodeAlreadyUsed(
        override val code: String = "CODE_ALREADY_USED",
        override val message: String,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AuthError(code, message, timestamp)

    data class UserBlocked(
        override val code: String = "USER_BLOCKED",
        override val message: String,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AuthError(code, message, timestamp)

    data class NetworkError(
        override val code: String = "NETWORK_ERROR",
        override val message: String,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AuthError(code, message, timestamp)

    data class ServiceUnavailable(
        override val code: String = "SERVICE_UNAVAILABLE",
        override val message: String,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AuthError(code, message, timestamp)

    data class UnknownError(
        override val code: String = "UNKNOWN_ERROR",
        override val message: String,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AuthError(code, message, timestamp)
}

