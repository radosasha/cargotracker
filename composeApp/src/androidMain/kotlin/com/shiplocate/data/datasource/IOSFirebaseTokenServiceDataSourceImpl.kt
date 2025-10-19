package com.shiplocate.data.datasource

/**
 * Android actual функция для запроса токена у Swift кода
 * На Android эта функция не используется, так как токен получается напрямую от Firebase
 */
actual fun requestTokenFromSwift() {
    println("Android: requestTokenFromSwift called - this should not happen on Android")
    // На Android токен получается напрямую от Firebase через getCurrentTokenFromPlatformImpl()
}
