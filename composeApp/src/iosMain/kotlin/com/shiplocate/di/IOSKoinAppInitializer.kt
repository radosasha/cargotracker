package com.shiplocate.di

import com.shiplocate.data.datasource.FirebaseTokenServiceDataSource
import com.shiplocate.domain.usecase.ManageFirebaseTokensUseCase
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
    private val firebaseTokenServiceDataSource: FirebaseTokenServiceDataSource by inject()
    
    /**
     * Инициализация зависимостей
     * Вызывается после инициализации Koin
     */
    fun initializeDependencies() {
        try {
            // Устанавливаем зависимости в IOSKoinApp
            IOSKoinApp.setDependencies(
                manageFirebaseTokensUseCase = manageFirebaseTokensUseCase,
                firebaseTokenServiceDataSource = firebaseTokenServiceDataSource
            )
            
            println("IOSKoinAppInitializer: Dependencies initialized successfully")
        } catch (e: Exception) {
            println("IOSKoinAppInitializer: Failed to initialize dependencies: ${e.message}")
        }
    }
}
