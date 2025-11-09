package com.shiplocate.data.services

import com.shiplocate.domain.model.FilteredLocationInfo
import com.shiplocate.domain.model.GpsLocation
import com.shiplocate.domain.model.LocationInfo
import com.shiplocate.domain.model.SendErrorInfo
import com.shiplocate.domain.model.TrackingStats
import com.shiplocate.domain.service.LocationProcessResult
import com.shiplocate.domain.service.LocationProcessor
import kotlinx.datetime.Clock

/**
 * Реализация LocationProcessor в data слое
 * Содержит бизнес-логику определения качества и необходимости отправки координат
 *
 * Находится в data слое, так как содержит mutable state
 */
class LocationProcessorImpl(
    val minSendIntervalMs: Long,  // 1 минута между отправками
    val minDistanceForSendM: Float, // 500 метров для отправки
    val maxAccuracyM: Float, // 70 метров максимальная точность
    // Принудительное сохранение каждые 30 минут
    val forceSaveIntervalMs: Long, // 30 минут
) : LocationProcessor {
    // Настройки фильтрации (можно вынести в конфигурацию)
    private var lastLocationSentTime = 0L
    private var lastLocationSent: GpsLocation? = null
    private var totalLocationsSent = 0
    private var totalLocationsReceived = 0
    private var lastForcedSaveTime = 0L

    // Статистика для уведомлений
    private var totalSaved = 0
    private var totalSent = 0
    private var totalFiltered = 0
    private var lastFilteredLocation: FilteredLocationInfo? = null
    private var lastSentLocation: LocationInfo? = null
    private var lastSendError: SendErrorInfo? = null

    /**
     * Обрабатывает новую GPS координату
     * @param location новая координата
     * @return результат обработки с информацией о том, была ли координата отправлена
     */
    override fun processLocation(location: GpsLocation): LocationProcessResult {
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

            val reason =
                when {
                    shouldForceSave -> "Forced save after 30 minutes"
                    else -> "Location meets criteria"
                }

            return LocationProcessResult(
                shouldSend = true,
                reason = reason,
                totalReceived = totalLocationsReceived,
                totalSent = totalLocationsSent,
                lastSentTime = lastLocationSentTime,
                trackingStats = createCurrentTrackingStats(),
                lastCoordinateLat = location.latitude,
                lastCoordinateLon = location.longitude,
                lastCoordinateTime = location.timestamp.toEpochMilliseconds(),
                coordinateErrorMeters = location.accuracy.toInt(),
            )
        } else {
            val filterReason = getFilterReason(location)

            // Обновляем статистику отфильтрованных координат
            totalFiltered++
            lastFilteredLocation =
                FilteredLocationInfo(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    timestamp = location.timestamp,
                    speed = location.speed,
                    altitude = location.altitude,
                    filterReason = filterReason,
                )

            return LocationProcessResult(
                shouldSend = false,
                reason = filterReason,
                totalReceived = totalLocationsReceived,
                totalSent = totalLocationsSent,
                lastSentTime = lastLocationSentTime,
                trackingStats = createCurrentTrackingStats(),
                lastCoordinateLat = location.latitude,
                lastCoordinateLon = location.longitude,
                lastCoordinateTime = location.timestamp.toEpochMilliseconds(),
                coordinateErrorMeters = location.accuracy.toInt(),
            )
        }
    }

    /**
     * Проверяет, нужно ли отправлять координаты на сервер
     * Координата отправляется если хотя бы одно из условий выполнилось:
     * - Прошло MIN_SEND_INTERVAL_MS ИЛИ
     * - Проехали MIN_DISTANCE_FOR_SEND_M
     */
    private fun shouldSendLocation(newLocation: GpsLocation): Boolean {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val lastSent = lastLocationSent

        // Проверяем точность (это обязательное условие)
        if (newLocation.accuracy > maxAccuracyM) {
            return false
        }

        // Проверяем интервал времени
        val timeIntervalPassed = (currentTime - lastLocationSentTime) >= minSendIntervalMs

        // Проверяем расстояние от последней отправленной координаты
        val distancePassed =
            if (lastSent != null) {
                val distance =
                    calculateDistance(
                        lastSent.latitude,
                        lastSent.longitude,
                        newLocation.latitude,
                        newLocation.longitude,
                    )
                distance >= minDistanceForSendM
            } else {
                // Если это первая координата, считаем что расстояние прошли
                true
            }

        // Отправляем если прошло время ИЛИ проехали расстояние
        return timeIntervalPassed || distancePassed
    }

    /**
     * Получает причину фильтрации координаты
     */
    private fun getFilterReason(location: GpsLocation): String {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val lastSent = lastLocationSent

        // Проверяем точность (обязательное условие)
        if (location.accuracy > maxAccuracyM) {
            return "Accuracy too low (${location.accuracy}m > ${maxAccuracyM}m)"
        }

        // Проверяем интервал времени
        val timeIntervalPassed = (currentTime - lastLocationSentTime) >= minSendIntervalMs

        // Проверяем расстояние
        val distancePassed =
            if (lastSent != null) {
                val distance =
                    calculateDistance(
                        lastSent.latitude,
                        lastSent.longitude,
                        location.latitude,
                        location.longitude,
                    )
                distance >= minDistanceForSendM
            } else {
                true
            }

        // Если ни одно условие не выполнено, объясняем почему
        return when {
            !timeIntervalPassed && !distancePassed -> {
                val timeLeft = minSendIntervalMs - (currentTime - lastLocationSentTime)
                val distance =
                    if (lastSent != null) {
                        calculateDistance(
                            lastSent.latitude,
                            lastSent.longitude,
                            location.latitude,
                            location.longitude,
                        )
                    } else {
                        0f
                    }
                val distanceLeft = minDistanceForSendM - distance
                "Too soon (${timeLeft}ms left) AND too close (${distanceLeft}m left)"
            }

            else -> "Unknown reason"
        }
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
        return (currentTime - lastForcedSaveTime) >= forceSaveIntervalMs
    }

    /**
     * Вычисляет расстояние между двумя точками (упрощенная формула)
     */
    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Float {
        val earthRadius = 6371000.0 // Радиус Земли в метрах
        val dLat = kotlin.math.PI / 180.0 * (lat2 - lat1)
        val dLon = kotlin.math.PI / 180.0 * (lon2 - lon1)
        val a =
            kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(kotlin.math.PI / 180.0 * lat1) * kotlin.math.cos(kotlin.math.PI / 180.0 * lat2) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }

    /**
     * Обновляет статистику сохранения координаты
     */
    override fun updateSavedLocation() {
        totalSaved++
    }

    /**
     * Обновляет статистику отправки координат
     */
    override fun updateSentLocations(
        location: GpsLocation,
        count: Int,
    ) {
        totalSent += count
        lastSentLocation =
            LocationInfo(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                timestamp = location.timestamp,
                speed = location.speed,
                altitude = location.altitude,
            )
        // Очищаем ошибку при успешной отправке
        lastSendError = null
    }

    /**
     * Обновляет статистику ошибки отправки координат
     */
    override fun updateSendError(
        location: GpsLocation,
        errorMessage: String,
        errorType: String,
    ) {
        lastSendError =
            SendErrorInfo(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                timestamp = location.timestamp,
                speed = location.speed,
                altitude = location.altitude,
                errorMessage = errorMessage,
                errorType = errorType,
            )
    }

    /**
     * Создает текущую статистику трекинга
     */
    override fun createCurrentTrackingStats(): TrackingStats {
        return TrackingStats(
            lastFilteredLocation = lastFilteredLocation,
            lastSentLocation = lastSentLocation,
            lastSendError = lastSendError,
            isTracking = true,
            totalSaved = totalSaved,
            totalSent = totalSent,
            totalFiltered = totalFiltered,
        )
    }
}
