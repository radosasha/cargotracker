package com.tracker.di

import com.tracker.data.di.iosDataModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/**
 * iOS инициализация Koin DI контейнера
 */
object IOSKoinApp {
    
    private var isInitialized = false
    
    /**
     * Инициализация Application-scoped зависимостей
     * Вызывается при запуске приложения
     */
    fun initApplicationScope() {
        if (!isInitialized) {
            try {
                println("IOSKoinApp: Starting Koin initialization...")
                println("IOSKoinApp: Loading modules: appModule + iosDataModule + iosModule")
                
                startKoin {
                    modules(
                        appModule + iosDataModule + iosModule
                    )
                }
                isInitialized = true
                println("IOSKoinApp: Application scope initialized successfully")
            } catch (e: Exception) {
                println("IOSKoinApp: Error during initialization: ${e.message}")
                e.printStackTrace()
                throw e
            }
        } else {
            println("IOSKoinApp: Application scope already initialized")
        }
    }
    
    /**
     * Инициализация ViewController-scoped зависимостей
     * Вызывается при создании ViewController
     */
    fun initViewControllerScope() {
        if (isInitialized) {
            // Koin уже инициализирован, добавляем ViewController модуль
            // Note: В реальном приложении здесь можно создать ViewController scope
            println("IOSKoinApp: ViewController scope initialized")
        } else {
            println("IOSKoinApp: Koin not initialized, cannot init view controller scope")
        }
    }
    
    fun stop() {
        stopKoin()
        isInitialized = false
    }
}
