package com.tracker

import com.tracker.di.IOSKoinApp

/**
 * iOS Application класс для инициализации Koin
 * Вызывается при запуске iOS приложения
 */
object IOSApplication {
    
    fun init() {
        // Инициализируем Koin с Application-scoped зависимостями
        IOSKoinApp.initApplicationScope()
        
        println("IOSApplication: Application initialized")
    }
}
