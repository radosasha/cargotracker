package com.shiplocate.domain.repository

import com.shiplocate.domain.model.auth.AuthToken
import com.shiplocate.domain.model.auth.SmsCodeRequest
import com.shiplocate.domain.model.auth.SmsCodeResponse
import com.shiplocate.domain.model.auth.SmsCodeVerify

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    /**
     * Request SMS verification code
     * @param request SMS code request with phone number
     * @return Result with response or error
     */
    suspend fun requestSmsCode(request: SmsCodeRequest): Result<SmsCodeResponse>

    /**
     * Verify SMS code and authenticate
     * @param verify SMS code verification data
     * @return Result with auth token or error
     */
    suspend fun verifySmsCode(verify: SmsCodeVerify): Result<AuthToken>
}
