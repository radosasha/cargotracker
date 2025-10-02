package com.tracker.domain.service

import com.tracker.domain.model.Location
import kotlinx.datetime.Clock

/**
 * Сервис для обработки и фильтрации GPS координат
 * Содержит бизнес-логику определения качества и необходимости отправки координат
 */
class LocationProcessor {
    
    // Настройки фильтрации (можно вынести в конфигурацию)
    private var lastLocationSentTime = 0L
    private var lastLocationSent: Location? = null
    private var totalLocationsSent = 0
    private var totalLocationsReceived = 0
    private var lastForcedSaveTime = 0L
    
    companion object {
        // Настройки отправки на сервер
        private const val MIN_SEND_INTERVAL_MS = 60 * 1000L // 1 минута между отправками
        private const val MIN_DISTANCE_FOR_SEND_M = 500f // 500 метров для отправки
        private const val MAX_ACCURACY_M = 70f // 70 метров максимальная точность
        
        // Принудительное сохранение каждые 30 минут
        private const val FORCE_SAVE_INTERVAL_MS = 30 * 60 * 1000L // 30 минут
    }
    
    /**
     * Обрабатывает новую GPS координату
     * @param location новая координата
     * @return результат обработки с информацией о том, была ли координата отправлена
     */
    fun processLocation(location: Location): LocationProcessResult {
        totalLocationsReceived++
        
        val currentTime = Clock.System.now().toEpochMilliseconds()
        
        // Проверяем принудительное сохранение (каждые 30 минут)
        val shouldForceSave = shouldForceSave(currentTime)
        
        // Проверяем, нужно ли отправлять координаты по обычным критериям
        val shouldSend = shouldSendLocation(location)
        
        if (shouldSend || shouldForceSave) {
            // Обновляем статистику
            totalLocationsSent++
            lastLocationSentTime = currentTime
            lastLocationSent = location
            
            if (shouldForceSave) {
                lastForcedSaveTime = currentTime
            }
            
            val reason = when {
                shouldForceSave -> "Forced save after 30 minutes"
                else -> "Location meets criteria"
            }
            
            return LocationProcessResult(
                shouldSend = true,
                reason = reason,
                totalReceived = totalLocationsReceived,
                totalSent = totalLocationsSent,
                lastSentTime = lastLocationSentTime
            )
        } else {
            return LocationProcessResult(
                shouldSend = false,
                reason = getFilterReason(location),
                totalReceived = totalLocationsReceived,
                totalSent = totalLocationsSent,
                lastSentTime = lastLocationSentTime
            )
        }
    }
    
    /**
     * Проверяет, нужно ли отправлять координаты на сервер
     */
    private fun shouldSendLocation(newLocation: Location): Boolean {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val lastSent = lastLocationSent
        
        // Проверяем интервал времени
        if (currentTime - lastLocationSentTime < MIN_SEND_INTERVAL_MS) {
            return false
        }
        
        // Проверяем точность
        if (newLocation.accuracy > MAX_ACCURACY_M) {
            return false
        }
        
        // Проверяем расстояние от последней отправленной координаты
        if (lastSent != null) {
            val distance = calculateDistance(
                lastSent.latitude, lastSent.longitude,
                newLocation.latitude, newLocation.longitude
            )
            if (distance < MIN_DISTANCE_FOR_SEND_M) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Получает причину фильтрации координаты
     */
    private fun getFilterReason(location: Location): String {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val lastSent = lastLocationSent
        
        // Проверяем интервал времени
        if (currentTime - lastLocationSentTime < MIN_SEND_INTERVAL_MS) {
            return "Too soon to send (${currentTime - lastLocationSentTime}ms < ${MIN_SEND_INTERVAL_MS}ms)"
        }
        
        // Проверяем точность
        if (location.accuracy > MAX_ACCURACY_M) {
            return "Accuracy too low (${location.accuracy}m > ${MAX_ACCURACY_M}m)"
        }
        
        // Проверяем расстояние
        if (lastSent != null) {
            val distance = calculateDistance(
                lastSent.latitude, lastSent.longitude,
                location.latitude, location.longitude
            )
            if (distance < MIN_DISTANCE_FOR_SEND_M) {
                return "Too close to last sent (${distance}m < ${MIN_DISTANCE_FOR_SEND_M}m)"
            }
        }
        
        return "Unknown reason"
    }
    
    /**
     * Проверяет, нужно ли принудительно сохранить координату
     * (если прошло 30 минут с последнего сохранения)
     */
    private fun shouldForceSave(currentTime: Long): Boolean {
        // Если lastForcedSaveTime = 0, инициализируем текущим временем
        if (lastForcedSaveTime == 0L) {
            lastForcedSaveTime = currentTime
            return false
        }
        
        // Проверяем, прошло ли 30 минут
        return (currentTime - lastForcedSaveTime) >= FORCE_SAVE_INTERVAL_MS
    }
    
    /**
     * Вычисляет расстояние между двумя точками (упрощенная формула)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val earthRadius = 6371000.0 // Радиус Земли в метрах
        val dLat = kotlin.math.PI / 180.0 * (lat2 - lat1)
        val dLon = kotlin.math.PI / 180.0 * (lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(kotlin.math.PI / 180.0 * lat1) * kotlin.math.cos(kotlin.math.PI / 180.0 * lat2) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }
    
    /**
     * Получает статистику обработки
     */
    fun getStats(): LocationProcessorStats {
        return LocationProcessorStats(
            totalReceived = totalLocationsReceived,
            totalSent = totalLocationsSent,
            lastSentTime = lastLocationSentTime,
            lastLocation = lastLocationSent
        )
    }
}

/**
 * Результат обработки GPS координаты
 */
data class LocationProcessResult(
    val shouldSend: Boolean,
    val reason: String,
    val totalReceived: Int,
    val totalSent: Int,
    val lastSentTime: Long
)

/**
 * Статистика обработки координат
 */
data class LocationProcessorStats(
    val totalReceived: Int,
    val totalSent: Int,
    val lastSentTime: Long,
    val lastLocation: Location?
)
