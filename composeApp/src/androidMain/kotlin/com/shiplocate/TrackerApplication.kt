package com.shiplocate

import android.app.Application
import com.shiplocate.di.AndroidKoinApp
import com.shiplocate.domain.usecase.ManageFirebaseTokensUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Application класс для инициализации Koin и Firebase Token Service
 */
class TrackerApplication : Application(), KoinComponent {
    private val manageFirebaseTokensUseCase: ManageFirebaseTokensUseCase by inject()

    // Application-scoped CoroutineScope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Инициализируем Application Context Provider
        ApplicationContextProvider.init(this)

        // Инициализируем Koin с Application-scoped зависимостями
        AndroidKoinApp.initApplicationScope(this)

        // Запускаем управление Firebase токенами
        // Используем Application-scoped CoroutineScope
        applicationScope.launch {
            manageFirebaseTokensUseCase.startManaging()
        }

        println("TrackerApplication: Application initialized with Firebase Token Management")
    }

    override fun onTerminate() {
        super.onTerminate()
        // Отменяем все корутины при завершении приложения
        applicationScope.cancel()
        println("TrackerApplication: Application scope cancelled")
    }
}
