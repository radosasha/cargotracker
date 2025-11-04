package com.shiplocate.domain.repository

import com.shiplocate.domain.model.PermissionStatus

/**
 * Интерфейс репозитория для работы с разрешениями
 */
interface PermissionRepository {
    /**
     * Получает текущий статус разрешений
     */
    suspend fun getPermissionStatus(): PermissionStatus

    /**
     * Запрашивает все необходимые разрешения
     */
    suspend fun requestAllPermissions(): Result<PermissionStatus>

    /**
     * Запрашивает разрешения на уведомления
     */
    suspend fun requestNotificationPermission(): Result<Boolean>
}
