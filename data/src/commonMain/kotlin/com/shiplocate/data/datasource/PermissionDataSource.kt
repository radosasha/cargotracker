package com.shiplocate.data.datasource

import com.shiplocate.data.model.PermissionDataModel

/**
 * Data Source для работы с разрешениями
 */
interface PermissionDataSource {
    /**
     * Получает текущий статус разрешений
     */
    suspend fun getPermissionStatus(): PermissionDataModel

    /**
     * Запрашивает все необходимые разрешения
     */
    suspend fun requestAllPermissions(): Result<PermissionDataModel>


    /**
     * Запрашивает разрешения на уведомления
     */
    suspend fun requestNotificationPermission(): Result<Boolean>
}
