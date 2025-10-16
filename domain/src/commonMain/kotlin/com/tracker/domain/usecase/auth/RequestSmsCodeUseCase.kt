package com.tracker.domain.usecase.auth

import com.tracker.domain.model.auth.SmsCodeRequest
import com.tracker.domain.model.auth.SmsCodeResponse
import com.tracker.domain.repository.AuthRepository

/**
 * Use case for requesting SMS verification code
 *
 * Business rules:
 * - Phone number must be valid (E.164 format)
 * - Rate limiting is enforced by backend (1min -> 1h -> 24h)
 * - SMS code is valid for 5 minutes
 */
class RequestSmsCodeUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(phone: String): Result<SmsCodeResponse> {
        println("📞 RequestSmsCodeUseCase: Requesting SMS for: $phone")

        // Validate phone number format on client side
        if (phone.isBlank()) {
            println("📞 RequestSmsCodeUseCase: ❌ Phone is blank")
            return Result.failure(IllegalArgumentException("Phone number cannot be empty"))
        }

        if (!phone.startsWith("+")) {
            println("📞 RequestSmsCodeUseCase: ❌ Phone doesn't start with '+'")
            return Result.failure(IllegalArgumentException("Phone number must start with '+'"))
        }

        val request = SmsCodeRequest(phone = phone)
        val result = authRepository.requestSmsCode(request)

        result.onSuccess {
            println("📞 RequestSmsCodeUseCase: ✅ Success: ${it.message}")
        }.onFailure {
            println("📞 RequestSmsCodeUseCase: ❌ Failed: ${it.message}")
        }

        return result
    }
}
