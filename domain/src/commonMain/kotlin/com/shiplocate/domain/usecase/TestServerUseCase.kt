package com.shiplocate.domain.usecase

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.Location
import com.shiplocate.domain.repository.LocationRepository
import kotlinx.datetime.Clock

/**
 * Use Case для тестирования подключения к серверу
 */
class TestServerUseCase(
    private val locationRepository: LocationRepository,
    private val logger: Logger,
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

            logger.info(LogCategory.NETWORK, "TestServerUseCase: Sending test location: ${testLocation.latitude}, ${testLocation.longitude}")

            // Отправляем тестовые координаты на сервер
//            locationRepository.sendLocation(testLocation)

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(LogCategory.NETWORK, "TestServerUseCase: Error sending test location: ${e.message}")
            Result.failure(e)
        }
    }
}
