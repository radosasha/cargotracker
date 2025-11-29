package com.shiplocate.domain.repository

import com.shiplocate.domain.model.PermissionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория для работы с разрешениями
 */
interface PermissionRepository {
    /**
     * Получает текущий статус разрешений
     */
    suspend fun getPermissionStatus(): PermissionStatus

    /**
     * Запрашивает разрешения на уведомления
     */
    suspend fun requestNotificationPermission(): Result<Boolean>

    /**
     * Запрашивает разрешение на точное местоположение (Precise Location)
     */
    suspend fun requestLocationPermission(): Result<PermissionStatus>

    /**
     * Запрашивает разрешение на фоновое местоположение (Background Location)
     */
    suspend fun requestBackgroundLocationPermission(): Result<PermissionStatus>

    /**
     * Запрашивает отключение оптимизации батареи (Unrestricted Battery Use)
     */
    suspend fun requestBatteryOptimizationDisable(): Result<PermissionStatus>

    /**
     * Запрашивает включение Location Services (GPS) через системный диалог
     */
    suspend fun requestEnableHighAccuracy(): Result<PermissionStatus>

    /**
     * Уведомляет о том, что разрешения были получены
     * Эмитит событие в Flow для подписчиков
     */
    suspend fun notifyPermissionGranted()

    /**
     * Возвращает Flow для наблюдения за изменениями статуса разрешений
     * Эмитит PermissionStatus при вызове notifyPermissionGranted()
     */
    fun observePermissions(): Flow<PermissionStatus>
}
