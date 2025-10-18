package com.tracker

import android.app.Application
import com.tracker.di.AndroidKoinApp

/**
 * Application класс для инициализации Koin
 */
class TrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Инициализируем Application Context Provider
        ApplicationContextProvider.init(this)

        // Инициализируем Koin с Application-scoped зависимостями
        AndroidKoinApp.initApplicationScope(this)

        println("TrackerApplication: Application initialized")
    }
}
