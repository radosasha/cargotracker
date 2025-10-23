package com.shiplocate.core.logging.di

import com.shiplocate.core.logging.CrashHandler
import com.shiplocate.core.logging.Logger
import com.shiplocate.core.logging.LoggingConfig
import com.shiplocate.core.logging.appender.FileAppender
import com.shiplocate.core.logging.files.FilesManager
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-специфичный модуль логирования
 */
actual val platformLoggingModule: Module =
    module {

        // Предоставляем iOS-специфичный FileAppender
        single<FileAppender> {
            val config = get<LoggingConfig>()
            FileAppender(config)
        }

        // Предоставляем iOS-специфичный FilesManager
        single<FilesManager> {
            FilesManager()
        }

        // iOS-специфичный обработчик крешей
        single<CrashHandler> {
            CrashHandler(get<Logger>())
        }
    }
