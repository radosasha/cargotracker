package com.tracker.domain.usecase.auth

import com.tracker.domain.model.auth.AuthToken
import com.tracker.domain.model.auth.SmsCodeVerify
import com.tracker.domain.repository.AuthRepository

/**
 * Use case for verifying SMS code and authenticating
 * 
 * Business rules:
 * - Code must be exactly 6 digits
 * - Code has 3 verification attempts
 * - Successful authentication invalidates previous sessions on other devices
 * - Token should be stored securely for subsequent API calls
 */
class VerifySmsCodeUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        phone: String,
        code: String,
        deviceInfo: String? = null
    ): Result<AuthToken> {
        println("🔓 VerifySmsCodeUseCase: Verifying code '$code' for phone: $phone")
        
        // Validate phone number format
        if (phone.isBlank()) {
            println("🔓 VerifySmsCodeUseCase: ❌ Phone is blank")
            return Result.failure(IllegalArgumentException("Phone number cannot be empty"))
        }
        
        if (!phone.startsWith("+")) {
            println("🔓 VerifySmsCodeUseCase: ❌ Phone doesn't start with '+'")
            return Result.failure(IllegalArgumentException("Phone number must start with '+'"))
        }
        
        // Validate code format
        if (code.isBlank()) {
            println("🔓 VerifySmsCodeUseCase: ❌ Code is blank")
            return Result.failure(IllegalArgumentException("Verification code cannot be empty"))
        }
        
        if (!code.matches(Regex("^\\d{6}$"))) {
            println("🔓 VerifySmsCodeUseCase: ❌ Code is not 6 digits")
            return Result.failure(IllegalArgumentException("Verification code must be exactly 6 digits"))
        }
        
        val verify = SmsCodeVerify(
            phone = phone,
            code = code,
            deviceInfo = deviceInfo
        )
        
        val result = authRepository.verifySmsCode(verify)
        
        result.onSuccess {
            println("🔓 VerifySmsCodeUseCase: ✅ Success: Token received for user ${it.user.name}")
        }.onFailure {
            println("🔓 VerifySmsCodeUseCase: ❌ Failed: ${it.message}")
        }
        
        return result
    }
}


