package com.shiplocate.domain.model.auth

/**
 * Domain model for authentication errors
 */
sealed class AuthError(
    open val code: String,
    override val message: String,
) : Exception(message) {
    companion object {
        // Standard error codes
        const val CODE_VALIDATION_ERROR = "VALIDATION_ERROR"
        const val CODE_RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED"
        const val CODE_INVALID = "CODE_INVALID"
        const val CODE_EXPIRED = "CODE_EXPIRED"
        const val CODE_NOT_FOUND = "CODE_NOT_FOUND"
        const val CODE_TOO_MANY_ATTEMPTS = "TOO_MANY_ATTEMPTS"
        const val CODE_ALREADY_USED = "CODE_ALREADY_USED"
        const val CODE_USER_BLOCKED = "USER_BLOCKED"
        const val CODE_NETWORK_ERROR = "NETWORK_ERROR"
        const val CODE_SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE"
        const val CODE_UNKNOWN_ERROR = "UNKNOWN_ERROR"
    }
    data class ValidationError(
        override val code: String = CODE_VALIDATION_ERROR,
        override val message: String,
    ) : AuthError(code, message)

    data class RateLimitError(
        override val code: String = CODE_RATE_LIMIT_EXCEEDED,
        override val message: String,
        val retryAfterSeconds: Long,
        val nextRetryAt: String? = null,
    ) : AuthError(code, message)

    data class CodeInvalid(
        override val code: String = CODE_INVALID,
        override val message: String,
        val remainingAttempts: Int? = null,
    ) : AuthError(code, message)

    data class CodeExpired(
        override val code: String = CODE_EXPIRED,
        override val message: String,
    ) : AuthError(code, message)

    data class CodeNotFound(
        override val code: String = CODE_NOT_FOUND,
        override val message: String,
    ) : AuthError(code, message)

    data class TooManyAttempts(
        override val code: String = CODE_TOO_MANY_ATTEMPTS,
        override val message: String,
    ) : AuthError(code, message)

    data class CodeAlreadyUsed(
        override val code: String = CODE_ALREADY_USED,
        override val message: String,
    ) : AuthError(code, message)

    data class UserBlocked(
        override val code: String = CODE_USER_BLOCKED,
        override val message: String,
    ) : AuthError(code, message)

    data class NetworkError(
        override val code: String = CODE_NETWORK_ERROR,
        override val message: String,
    ) : AuthError(code, message)

    data class ServiceUnavailable(
        override val code: String = CODE_SERVICE_UNAVAILABLE,
        override val message: String,
    ) : AuthError(code, message)

    data class UnknownError(
        override val code: String = CODE_UNKNOWN_ERROR,
        override val message: String,
    ) : AuthError(code, message)
}
