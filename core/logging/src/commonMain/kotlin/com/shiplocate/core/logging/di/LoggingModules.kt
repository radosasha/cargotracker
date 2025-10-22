package com.shiplocate.core.logging.di

import org.koin.core.module.Module

/**
 * Экспорт всех модулей логирования для удобного импорта
 */
object LoggingModules {
    val all: List<Module>
        get() = listOf(
            loggingModule,
            platformLoggingModule,
        )
}

/**
 * Платформенный модуль логирования
 */
expect val platformLoggingModule: Module
