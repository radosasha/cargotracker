package com.shiplocate.core.logging.di

import com.shiplocate.core.logging.CrashHandler
import com.shiplocate.core.logging.Logger
import com.shiplocate.core.logging.LoggingConfig
import com.shiplocate.core.logging.appender.FileAppender
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-специфичный модуль логирования
 */
actual val platformLoggingModule: Module =
    module {

        // Предоставляем Android-специфичный FileAppender
        single<FileAppender> {
            FileAppender(get(), get())
        }

        // Android-специфичный обработчик крешей
        single<CrashHandler> {
            CrashHandler(androidApplication(), get<Logger>())
        }
    }
