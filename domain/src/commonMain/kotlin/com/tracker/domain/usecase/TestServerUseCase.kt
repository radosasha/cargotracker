package com.tracker.domain.usecase

import com.tracker.domain.model.Location
import com.tracker.domain.repository.LocationRepository
import kotlinx.datetime.Clock

/**
 * Use Case для тестирования подключения к серверу
 */
class TestServerUseCase(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Создаем тестовые координаты (Москва)
            val testLocation =
                Location(
                    latitude = 55.7558,
                    longitude = 37.6176,
                    timestamp = Clock.System.now(),
                    accuracy = 10f,
                    altitude = 150.0,
                    speed = 0f,
                    bearing = 0f,
                    deviceId = "40329715",
                )

            println("TestServerUseCase: Sending test location: ${testLocation.latitude}, ${testLocation.longitude}")

            // Отправляем тестовые координаты на сервер
//            locationRepository.sendLocation(testLocation)

            Result.success(Unit)
        } catch (e: Exception) {
            println("TestServerUseCase: Error sending test location: ${e.message}")
            Result.failure(e)
        }
    }
}
