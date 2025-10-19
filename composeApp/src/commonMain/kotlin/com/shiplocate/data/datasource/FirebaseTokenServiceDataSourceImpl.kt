package com.shiplocate.data.datasource

/**
 * Платформо-специфичная функция для получения текущего Firebase токена
 */
expect suspend fun getCurrentTokenFromPlatformImpl(): String?
