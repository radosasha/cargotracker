package com.shiplocate.di

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.repository.NotificationRepository
import com.shiplocate.domain.usecase.HandlePushNotificationWhenAppKilledUseCase
import com.shiplocate.domain.usecase.ManageFirebaseTokensUseCase
import com.shiplocate.domain.usecase.auth.HasAuthSessionUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOS Koin App Initializer
 * 
 * Следует принципам Clean Architecture и SOLID:
 * - Внедряет зависимости через конструкторы
 * - Не использует GlobalContext напрямую
 * - Создает зависимости через Koin модули
 */
class IOSKoinAppInitializer : KoinComponent {
    
    private val manageFirebaseTokensUseCase: ManageFirebaseTokensUseCase by inject()
    private val notificationRepository: NotificationRepository by inject()
    private val handlePushNotificationWhenAppKilledUseCase: HandlePushNotificationWhenAppKilledUseCase by inject()
    private val hasAuthUseCase: HasAuthSessionUseCase by inject()
    private val logger: Logger by inject()
    
    /**
     * Инициализация зависимостей
     * Вызывается после инициализации Koin
     */
    fun initializeDependencies() {
        try {
            // Устанавливаем зависимости в IOSKoinApp
            IOSKoinApp.setDependencies(
                manageFirebaseTokensUseCase = manageFirebaseTokensUseCase,
                notificationRepository = notificationRepository,
                handlePushNotificationWhenAppKilledUseCase = handlePushNotificationWhenAppKilledUseCase,
                hasAuthUseCase = hasAuthUseCase,
                logger = logger
            )
            
            logger.info(LogCategory.GENERAL, "IOSKoinAppInitializer: Dependencies initialized successfully")
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "IOSKoinAppInitializer: Failed to initialize dependencies: ${e.message}")
        }
    }
}
