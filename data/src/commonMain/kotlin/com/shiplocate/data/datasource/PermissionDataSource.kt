package com.shiplocate.data.datasource

import com.shiplocate.data.model.PermissionDataModel
import kotlinx.coroutines.flow.Flow

/**
 * Data Source для работы с разрешениями
 */
interface PermissionDataSource {
    /**
     * Получает текущий статус разрешений
     */
    suspend fun getPermissionStatus(): PermissionDataModel


    /**
     * Запрашивает разрешения на уведомления
     */
    suspend fun requestNotificationPermission(): Result<Boolean>

    /**
     * Запрашивает разрешение на точное местоположение (Precise Location)
     */
    suspend fun requestLocationPermission(): Result<PermissionDataModel>

    /**
     * Запрашивает разрешение на фоновое местоположение (Background Location)
     */
    suspend fun requestBackgroundLocationPermission(): Result<PermissionDataModel>

    /**
     * Запрашивает отключение оптимизации батареи (Unrestricted Battery Use)
     */
    suspend fun requestBatteryOptimizationDisable(): Result<PermissionDataModel>

    /**
     * Запрашивает включение GPS через системный диалог
     */
    suspend fun requestEnableHighAccuracy(): Result<PermissionDataModel>

    /**
     * Открывает настройки режима полета
     */
    suspend fun openAirplaneModeSettings(): Result<Unit>

    /**
     * Уведомляет о том, что разрешения были получены
     * Эмитит событие в Flow для подписчиков
     */
    suspend fun notifyPermissionGranted()

    /**
     * Возвращает Flow для наблюдения за изменениями статуса разрешений
     * Эмитит PermissionDataModel при вызове notifyPermissionGranted()
     */
    fun observePermissions(): Flow<PermissionDataModel>
}
