package com.shiplocate.di

import com.shiplocate.data.service.platform.IOSFirebaseMessagingDelegate
import com.shiplocate.data.datasource.FirebaseTokenServiceDataSource
import com.shiplocate.domain.usecase.ManageFirebaseTokensUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/**
 * iOS инициализация Koin DI контейнера
 *
 * Использует флаги состояния для отслеживания инициализации,
 * вместо try-catch блоков (как в серьезных компаниях)
 */
object IOSKoinApp : KoinComponent {
    // Флаги состояния
    private var hasViewControllerScope = false

    private val manageFirebaseTokensUseCase: ManageFirebaseTokensUseCase by inject()

    // Application-scoped CoroutineScope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Инициализация Application-scoped зависимостей
     * Вызывается при запуске приложения
     */
    fun initApplicationScope() {
        println("IOSKoinApp: Starting Koin initialization...")
        println("IOSKoinApp: Loading modules: appModule + iosModule")

        startKoin {
            printLogger() // Включаем логирование Koin
            modules(
                appModule + iosModule + iosPlatformModule,
            )
        }

        println("IOSKoinApp: Application scope initialized successfully")
    }

    /**
     * Запустить управление Firebase токенами для iOS
     */
    fun startFirebaseTokenService() {
        try {
            // Используем Application-scoped CoroutineScope
            applicationScope.launch {
                manageFirebaseTokensUseCase.startManaging()
            }
            println("IOSKoinApp: Firebase Token Management started successfully")
        } catch (e: Exception) {
            println("IOSKoinApp: Failed to start Firebase Token Management: ${e.message}")
        }
    }

    /**
     * Инициализация ViewController-scoped зависимостей
     * Вызывается при создании ViewController
     */
    fun initViewControllerScope() {
        if (hasViewControllerScope) {
            println("IOSKoinApp: ViewController scope already initialized, skipping")
            return
        }

        hasViewControllerScope = true
        println("IOSKoinApp: ViewController scope initialized successfully")
    }

    /**
     * Остановка Koin
     * Вызывается при завершении приложения
     */
    fun stop() {
        // Отменяем все корутины при завершении приложения
        applicationScope.cancel()
        stopKoin()
        hasViewControllerScope = false
        println("IOSKoinApp: Stopped successfully")
    }

    /**
     * Проверка, инициализирован ли ViewController scope
     */
    fun hasViewControllerScopeInitialized(): Boolean = hasViewControllerScope

    /**
     * Получение нового Firebase токена от iOS
     * Вызывается из iOSApp.swift
     */
    fun onNewTokenReceived(token: String) {
        try {
            // Получаем IOSFirebaseMessagingDelegate и передаем токен
            val delegate = get<IOSFirebaseMessagingDelegate>()
            delegate.onNewTokenReceived(token)
            println("IOSKoinApp: Token passed to delegate successfully")
        } catch (e: Exception) {
            println("IOSKoinApp: Failed to pass token to delegate: ${e.message}")
        }
    }
    
    /**
     * Получение текущего Firebase токена от iOS
     * Вызывается из iOSApp.swift при запросе токена
     */
    fun onCurrentTokenReceived(token: String?) {
        try {
            // Получаем FirebaseTokenServiceDataSource и передаем токен
            val dataSource = get<FirebaseTokenServiceDataSource>()
            dataSource.onTokenReceived(token)
            println("IOSKoinApp: Current token passed to data source successfully")
        } catch (e: Exception) {
            println("IOSKoinApp: Failed to pass current token to data source: ${e.message}")
        }
    }
}
