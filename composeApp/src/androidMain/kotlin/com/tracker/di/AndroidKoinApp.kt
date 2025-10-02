package com.tracker.di

import com.tracker.ActivityContextProvider
import com.tracker.data.di.androidDataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.context.GlobalContext

/**
 * Android инициализация Koin DI контейнера
 */
object AndroidKoinApp {
    
    /**
     * Инициализация Application-scoped зависимостей
     * Вызывается в Application.onCreate()
     */
    fun initApplicationScope(application: android.app.Application) {
        // Проверяем, запущен ли Koin уже
        try {
            GlobalContext.get()
            // Если дошли сюда, значит Koin уже инициализирован
        } catch (e: Exception) {
            // Koin не инициализирован, запускаем его
            startKoin {
                androidContext(application)
                modules(
                    appModule + androidDataModule + activityModule
                )
            }
        }
    }
    
    /**
     * Инициализация Activity-scoped зависимостей
     * Вызывается в Activity.onCreate()
     */
    fun initActivityScope() {
        // Проверяем, что Koin инициализирован
        try {
            GlobalContext.get()
            // Koin уже инициализирован, добавляем Activity модуль
            // Note: В реальном приложении здесь можно создать Activity scope
        } catch (e: Exception) {
            println("AndroidKoinApp: Koin not initialized, cannot init activity scope")
        }
    }
    
    fun initActivityContext(context: android.content.Context) {
        // Проверяем, что Koin инициализирован
        try {
            val activityContextProvider = GlobalContext.get().get<ActivityContextProvider>()
            activityContextProvider.setContext(context)
        } catch (e: Exception) {
            // Koin не инициализирован, пропускаем
            println("AndroidKoinApp: Koin not initialized, skipping activity context init")
        }
    }
    
    fun clearActivityContext() {
        // Проверяем, что Koin инициализирован
        try {
            val activityContextProvider = GlobalContext.get().get<ActivityContextProvider>()
            activityContextProvider.clearContext()
        } catch (e: Exception) {
            // Koin не инициализирован, пропускаем
            println("AndroidKoinApp: Koin not initialized, skipping activity context clear")
        }
    }
    
    fun stop() {
        clearActivityContext()
        stopKoin()
    }
}

