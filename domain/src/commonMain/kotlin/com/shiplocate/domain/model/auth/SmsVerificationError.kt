package com.shiplocate.domain.model.auth

/**
 * Domain model for SMS verification errors
 * Contains only errors that can be returned from MobileAuthResource.verifyCode endpoint
 */
sealed class SmsVerificationError(
    open val code: String,
    override val message: String,
) : Exception(message) {
    companion object {
        // Verification error codes from VerificationErrorCode
        const val CODE_NOT_FOUND = "CODE_NOT_FOUND"
        const val CODE_EXPIRED = "CODE_EXPIRED"
        const val CODE_INVALID = "CODE_INVALID"
        const val CODE_ALREADY_USED = "CODE_ALREADY_USED"
        const val TOO_MANY_ATTEMPTS = "TOO_MANY_ATTEMPTS"
        const val RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED"
        const val INVALID_PHONE = "INVALID_PHONE"
        const val VERIFICATION_ERROR = "VERIFICATION_ERROR"
        const val VERIFICATION_FAILED = "VERIFICATION_FAILED"
        
        // Other error codes from verifyCode endpoint
        const val VALIDATION_ERROR = "VALIDATION_ERROR"
        const val USER_BLOCKED = "USER_BLOCKED"
        const val INTERNAL_ERROR = "INTERNAL_ERROR"
    }

    /**
     * Code not found or expired
     */
    data class CodeNotFound(
        override val code: String = CODE_NOT_FOUND,
        override val message: String,
    ) : SmsVerificationError(code, message)

    /**
     * Code expired
     */
    data class CodeExpired(
        override val code: String = CODE_EXPIRED,
        override val message: String,
    ) : SmsVerificationError(code, message)

    /**
     * Invalid code with remaining attempts
     */
    data class CodeInvalid(
        override val code: String = CODE_INVALID,
        override val message: String,
        val remainingAttempts: Int? = null,
    ) : SmsVerificationError(code, message)

    /**
     * Too many failed attempts
     */
    data class TooManyAttempts(
        override val code: String = TOO_MANY_ATTEMPTS,
        override val message: String,
    ) : SmsVerificationError(code, message)

    /**
     * Code already used
     */
    data class CodeAlreadyUsed(
        override val code: String = CODE_ALREADY_USED,
        override val message: String,
    ) : SmsVerificationError(code, message)

    /**
     * Rate limit exceeded (with Twilio error code)
     */
    data class RateLimitExceeded(
        override val code: String = RATE_LIMIT_EXCEEDED,
        override val message: String,
        val twilioErrorCode: String? = null,
    ) : SmsVerificationError(code, message)

    /**
     * Invalid phone number (with Twilio error code)
     */
    data class InvalidPhone(
        override val code: String = INVALID_PHONE,
        override val message: String,
        val twilioErrorCode: String? = null,
    ) : SmsVerificationError(code, message)

    /**
     * Verification service error (with Twilio error code)
     */
    data class VerificationServiceError(
        override val code: String = VERIFICATION_ERROR,
        override val message: String,
        val twilioErrorCode: String? = null,
    ) : SmsVerificationError(code, message)

    /**
     * General verification failed
     */
    data class VerificationFailed(
        override val code: String = VERIFICATION_FAILED,
        override val message: String,
    ) : SmsVerificationError(code, message)

    /**
     * Validation error
     */
    data class ValidationError(
        override val code: String = VALIDATION_ERROR,
        override val message: String,
    ) : SmsVerificationError(code, message)

    /**
     * User account blocked
     */
    data class UserBlocked(
        override val code: String = USER_BLOCKED,
        override val message: String,
    ) : SmsVerificationError(code, message)

    /**
     * Internal server error
     */
    data class InternalError(
        override val code: String = INTERNAL_ERROR,
        override val message: String,
    ) : SmsVerificationError(code, message)
}

