package com.shiplocate.domain.service

/**
 * Интерфейс для управления синхронизацией неотправленных координат
 */
interface LocationSyncService {
    /**
     * Запускает периодическую синхронизацию неотправленных координат
     * @param loadId ID загрузки для трекинга
     */
    fun startSync()

    /**
     * Останавливает периодическую синхронизацию
     */
    fun stopSync()

    /**
     * Проверяет, запущена ли синхронизация
     */
    fun isSyncActive(): Boolean
}
