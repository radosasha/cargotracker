package com.shiplocate.trackingsdk.geofence

import kotlinx.coroutines.flow.Flow

/**
 * Expect/actual интерфейс для GeofenceClient
 * Предоставляет платформо-специфичную реализацию отслеживания геозон
 */
expect class GeofenceClient {
    /**
     * Добавляет геозону для отслеживания
     * @param stop Stop для которого создается геозона
     */
    suspend fun addGeofence(id: Long, latitude: Double, longitude: Double, radius: Int)

    /**
     * Удаляет геозону
     * @param stopId ID стопа для которого удаляется геозона
     */
    suspend fun removeGeofence(id: Long)

    /**
     * Удаляет все геозоны
     */
    suspend fun removeAllGeofences()

    /**
     * Поток событий геозон (вход/выход)
     */
    fun observeGeofenceEvents(): Flow<GeofenceEvent>
}

/**
 * Событие геозоны
 */
sealed interface GeofenceEvent {
    /**
     * Вход в геозону
     */
    data class Entered(
        val stopId: Long,
        val stopType: Int,
    ) : GeofenceEvent

    /**
     * Выход из геозоны
     */
    data class Exited(
        val stopId: Long,
        val stopType: Int,
    ) : GeofenceEvent
}

