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
    
    fun init() {
        startKoin {
            modules(
                appModule + androidDataModule + activityModule
            )
        }
    }
    
    fun initActivityScope(context: android.content.Context) {
        // Получаем ActivityContextProvider из Koin и устанавливаем контекст
        val activityContextProvider = GlobalContext.get().get<ActivityContextProvider>()
        activityContextProvider.setContext(context)
    }
    
    fun clearActivityScope() {
        // Получаем ActivityContextProvider из Koin и очищаем контекст
        val activityContextProvider = GlobalContext.get().get<ActivityContextProvider>()
        activityContextProvider.clearContext()
    }
    
    fun stop() {
        clearActivityScope()
        stopKoin()
    }
}

