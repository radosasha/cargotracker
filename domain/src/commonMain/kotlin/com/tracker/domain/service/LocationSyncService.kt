package com.tracker.domain.service

/**
 * Интерфейс для управления синхронизацией неотправленных координат
 */
interface LocationSyncService {
    /**
     * Запускает периодическую синхронизацию неотправленных координат
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
