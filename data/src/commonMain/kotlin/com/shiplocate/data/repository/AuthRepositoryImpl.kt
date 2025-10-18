package com.shiplocate.data.repository

import com.shiplocate.data.datasource.remote.AuthRemoteDataSource
import com.shiplocate.domain.model.auth.AuthToken
import com.shiplocate.domain.model.auth.SmsCodeRequest
import com.shiplocate.domain.model.auth.SmsCodeResponse
import com.shiplocate.domain.model.auth.SmsCodeVerify
import com.shiplocate.domain.repository.AuthRepository

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
