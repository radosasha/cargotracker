package com.shiplocate.di

import android.content.Context
import com.shiplocate.ActivityProvider
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.di.trackingSDKModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

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
            printLogger()
            androidContext(application)
            modules(
                appModule + activityModule + androidPlatformModule  + trackingSDKModule,
            )
        }
    }

    /**
     * Инициализация Activity-scoped зависимостей
     * Вызывается в Activity.onCreate()
     */
    fun initActivityScope(logger: Logger? = null) {
        if (hasActivityScope) {
            logger?.info(LogCategory.GENERAL, "AndroidKoinApp: Activity scope already initialized, skipping")
                ?: println("AndroidKoinApp: Activity scope already initialized, skipping")
            return
        }

        hasActivityScope = true
        logger?.info(LogCategory.GENERAL, "AndroidKoinApp: Activity scope initialized successfully")
            ?: println("AndroidKoinApp: Activity scope initialized successfully")
    }

    /**
     * Установка Activity контекста
     * Вызывается в Activity.onCreate()
     */
    fun initActivityContext(context: Context, logger: Logger? = null) {
        val activityContextProvider = GlobalContext.get().get<ActivityProvider>()
        activityContextProvider.setActivity(context)
        logger?.info(LogCategory.GENERAL, "AndroidKoinApp: Activity context set successfully")
            ?: println("AndroidKoinApp: Activity context set successfully")
    }

    /**
     * Очистка Activity контекста
     * Вызывается в Activity.onDestroy()
     */
    fun clearActivityContext(logger: Logger? = null) {
        val activityContextProvider = GlobalContext.get().get<ActivityProvider>()
        activityContextProvider.clearActivity()
        hasActivityScope = false
        logger?.info(LogCategory.GENERAL, "AndroidKoinApp: Activity context cleared successfully")
            ?: println("AndroidKoinApp: Activity context cleared successfully")
    }

    /**
     * Остановка Koin
     * Вызывается при завершении приложения
     */
    fun stop(logger: Logger? = null) {
        clearActivityContext(logger)
        stopKoin()
        hasActivityScope = false
        logger?.info(LogCategory.GENERAL, "AndroidKoinApp: Stopped successfully")
            ?: println("AndroidKoinApp: Stopped successfully")
    }

    /**
     * Проверка, инициализирован ли Activity scope
     */
    fun hasActivityScopeInitialized(): Boolean = hasActivityScope
}
