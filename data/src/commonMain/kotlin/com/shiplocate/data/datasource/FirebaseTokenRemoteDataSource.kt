package com.shiplocate.data.datasource

interface FirebaseTokenRemoteDataSource {
    suspend fun sendTokenToServer(token: String)

    suspend fun clearToken()
}
