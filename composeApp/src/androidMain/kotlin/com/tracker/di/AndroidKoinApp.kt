package com.tracker.di

import com.tracker.ActivityProvider
import com.tracker.data.di.androidDataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.context.GlobalContext

/**
 * Android инициализация Koin DI контейнера
 * 
 * Использует флаги состояния для отслеживания инициализации,
 * вместо try-catch блоков (как в серьезных компаниях)
 */
object AndroidKoinApp {

    private var hasActivityScope = false
    
    /**
     * Инициализация Application-scoped зависимостей
     * Вызывается в Application.onCreate()
     */
    fun initApplicationScope(application: android.app.Application) {
        
        startKoin {
            androidContext(application)
            modules(
                appModule + androidDataModule + activityModule
            )
        }

        println("AndroidKoinApp: Application scope initialized successfully")
    }
    
    /**
     * Инициализация Activity-scoped зависимостей
     * Вызывается в Activity.onCreate()
     */
    fun initActivityScope() {
        
        if (hasActivityScope) {
            println("AndroidKoinApp: Activity scope already initialized, skipping")
            return
        }
        
        hasActivityScope = true
        println("AndroidKoinApp: Activity scope initialized successfully")
    }
    
    /**
     * Установка Activity контекста
     * Вызывается в Activity.onCreate()
     */
    fun initActivityContext(context: android.content.Context) {
        
        val activityContextProvider = GlobalContext.get().get<ActivityProvider>()
        activityContextProvider.setActivity(context)
        println("AndroidKoinApp: Activity context set successfully")
    }
    
    /**
     * Очистка Activity контекста
     * Вызывается в Activity.onDestroy()
     */
    fun clearActivityContext() {
        val activityContextProvider = GlobalContext.get().get<ActivityProvider>()
        activityContextProvider.clearActivity()
        hasActivityScope = false
        println("AndroidKoinApp: Activity context cleared successfully")
    }
    
    /**
     * Остановка Koin
     * Вызывается при завершении приложения
     */
    fun stop() {
        clearActivityContext()
        stopKoin()
        hasActivityScope = false
        println("AndroidKoinApp: Stopped successfully")
    }
    
    /**
     * Проверка, инициализирован ли Activity scope
     */
    fun hasActivityScopeInitialized(): Boolean = hasActivityScope
}

