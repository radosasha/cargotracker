package com.tracker.data.repository

import com.tracker.data.datasource.remote.AuthRemoteDataSource
import com.tracker.domain.model.auth.AuthToken
import com.tracker.domain.model.auth.SmsCodeRequest
import com.tracker.domain.model.auth.SmsCodeResponse
import com.tracker.domain.model.auth.SmsCodeVerify
import com.tracker.domain.repository.AuthRepository

/**
 * Implementation of AuthRepository
 * Delegates to AuthRemoteDataSource for API calls
 */
class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override suspend fun requestSmsCode(request: SmsCodeRequest): Result<SmsCodeResponse> {
        return remoteDataSource.requestSmsCode(request)
    }

    override suspend fun verifySmsCode(verify: SmsCodeVerify): Result<AuthToken> {
        return remoteDataSource.verifySmsCode(verify)
    }
}
