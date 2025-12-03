package com.shiplocate.domain.model.auth

/**
 * Domain model for SMS request errors
 * Contains only errors that can be returned from MobileAuthResource.requestCode endpoint
 * Based on actual server responses:
 * - 400: VALIDATION_ERROR
 * - 429: RATE_LIMIT_EXCEEDED (with retryAfterSeconds, nextRetryAt)
 * - 500: DATABASE_ERROR
 * - 503: SMS_SERVICE_ERROR
 */
sealed class SmsRequestError(
    open val code: String,
    override val message: String,
) : Exception(message) {
    companion object {
        const val VALIDATION_ERROR = "VALIDATION_ERROR"
        const val RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED"
        const val DATABASE_ERROR = "DATABASE_ERROR"
        const val SMS_SERVICE_ERROR = "SMS_SERVICE_ERROR"
    }

    /**
     * Validation error (400 Bad Request)
     */
    data class ValidationError(
        override val code: String = VALIDATION_ERROR,
        override val message: String,
    ) : SmsRequestError(code, message)

    /**
     * Rate limit exceeded (429 Too Many Requests)
     */
    data class RateLimitExceeded(
        override val code: String = RATE_LIMIT_EXCEEDED,
        override val message: String,
        val retryAfterSeconds: Long? = null,
        val nextRetryAt: String? = null,
    ) : SmsRequestError(code, message)

    /**
     * SMS service error (503 Service Unavailable)
     */
    data class SmsServiceError(
        override val code: String = SMS_SERVICE_ERROR,
        override val message: String,
    ) : SmsRequestError(code, message)
}


