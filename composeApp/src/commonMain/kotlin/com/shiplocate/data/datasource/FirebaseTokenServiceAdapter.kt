package com.shiplocate.data.datasource

import kotlinx.coroutines.flow.Flow

/**
 * Адаптер для связи интерфейса FirebaseTokenService с expect классом FirebaseTokenServiceDataSource
 */
class FirebaseTokenServiceAdapter(
    private val firebaseTokenServiceDataSource: FirebaseTokenServiceDataSource
) : FirebaseTokenService {
    
    override suspend fun getCurrentToken(): String? {
        return firebaseTokenServiceDataSource.getCurrentToken()
    }
    
    override fun getNewTokenFlow(): Flow<String> {
        return firebaseTokenServiceDataSource.getNewTokenFlow()
    }
    
    override suspend fun onNewTokenReceived(token: String) {
        firebaseTokenServiceDataSource.onNewTokenReceived(token)
    }
}
