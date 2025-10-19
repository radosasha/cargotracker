package com.shiplocate.data.datasource

interface FirebaseTokenRemoteDataSource {
    suspend fun sendTokenToServer(token: String)

    suspend fun getTokenStatus(): Boolean

    suspend fun clearToken()
}
