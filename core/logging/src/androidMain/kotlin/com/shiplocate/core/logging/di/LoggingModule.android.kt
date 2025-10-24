package com.shiplocate.core.logging.di

import com.shiplocate.core.logging.CrashHandler
import com.shiplocate.core.logging.Logger
import com.shiplocate.core.logging.LoggingConfig
import com.shiplocate.core.logging.LogsSettings
import com.shiplocate.core.logging.appender.FileAppender
import com.shiplocate.core.logging.files.FilesManager
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-специфичный модуль логирования
 */
actual val platformLoggingModule: Module =
    module {

        // Предоставляем Android-специфичный FilesManager
        single<FilesManager> {
            FilesManager(androidApplication())
        }

        // Предоставляем Android-специфичный LogsSettings
        single<LogsSettings> {
            LogsSettings(androidApplication())
        }

        // Предоставляем Android-специфичный FileAppender
        single<FileAppender> {
            FileAppender(get<LoggingConfig>(), get<FilesManager>(), get<LogsSettings>())
        }

        // Android-специфичный обработчик крешей
        single<CrashHandler> {
            CrashHandler(androidApplication(), get<Logger>())
        }
    }
