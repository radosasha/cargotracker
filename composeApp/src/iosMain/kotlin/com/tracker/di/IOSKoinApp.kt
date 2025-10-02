package com.tracker.di

import com.tracker.data.di.iosDataModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/**
 * iOS инициализация Koin DI контейнера
 * 
 * Использует флаги состояния для отслеживания инициализации,
 * вместо try-catch блоков (как в серьезных компаниях)
 */
object IOSKoinApp {
    
    // Флаги состояния
    private var hasViewControllerScope = false
    
    /**
     * Инициализация Application-scoped зависимостей
     * Вызывается при запуске приложения
     */
    fun initApplicationScope() {
        
        println("IOSKoinApp: Starting Koin initialization...")
        println("IOSKoinApp: Loading modules: appModule + iosModule + iosDataModule")
        
        startKoin {
            printLogger() // Включаем логирование Koin
            modules(
                appModule + iosModule + iosDataModule
            )
        }

        println("IOSKoinApp: Application scope initialized successfully")
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
        
        stopKoin()
        hasViewControllerScope = false
        println("IOSKoinApp: Stopped successfully")
    }
    
    /**
     * Проверка, инициализирован ли ViewController scope
     */
    fun hasViewControllerScopeInitialized(): Boolean = hasViewControllerScope
}
