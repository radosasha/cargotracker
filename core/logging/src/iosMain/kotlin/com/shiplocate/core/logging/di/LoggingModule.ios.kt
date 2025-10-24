package com.shiplocate.core.logging.di

import com.shiplocate.core.logging.CrashHandler
import com.shiplocate.core.logging.Logger
import com.shiplocate.core.logging.LoggingConfig
import com.shiplocate.core.logging.LogsSettings
import com.shiplocate.core.logging.appender.FileAppender
import com.shiplocate.core.logging.files.FilesManager
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-специфичный модуль логирования
 */
actual val platformLoggingModule: Module =
    module {

        // Предоставляем iOS-специфичный FilesManager
        single<FilesManager> {
            FilesManager()
        }

        // Предоставляем iOS-специфичный LogsSettings
        single<LogsSettings> {
            LogsSettings()
        }

        // Предоставляем iOS-специфичный FileAppender
        single<FileAppender> {
            FileAppender(get<LoggingConfig>(), get<FilesManager>(), get<LogsSettings>())
        }

        // iOS-специфичный обработчик крешей
        single<CrashHandler> {
            CrashHandler(get<Logger>())
        }
    }
