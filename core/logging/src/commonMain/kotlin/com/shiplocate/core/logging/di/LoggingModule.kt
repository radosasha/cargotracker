package com.shiplocate.core.logging.di

import com.shiplocate.core.logging.LogLevel
import com.shiplocate.core.logging.Logger
import com.shiplocate.core.logging.LoggerBuilder
import com.shiplocate.core.logging.LoggerImpl
import com.shiplocate.core.logging.LoggingConfig
import com.shiplocate.core.logging.appender.ConsoleAppender
import com.shiplocate.core.logging.appender.FileAppender
import com.shiplocate.core.logging.appender.NetworkAppender
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Общий модуль логирования для всех платформ
 */
val loggingModule: Module =
    module {

        // Предоставляем конфигурацию логирования
        single<LoggingConfig> {
            LoggerBuilder()
                .setLogLevel(LogLevel.DEBUG)
                .enableConsoleLogging()
                .enableFileLogging()
                .logNetworkRequests()
                .logStackTraces()
                .logDatabaseQueries()
                .enableUserUiInteractions()
                .crashErrorLogging()
                .setFileHours(true)
                .setMaxFileSize(3 * 1024 * 1024) // 3MB
                .setMaxFiles(48)
                .buildConfig()
        }

        // Предоставляем appenders
        single<ConsoleAppender> { ConsoleAppender() }
        single<NetworkAppender> { NetworkAppender() }
        
        // FileAppender будет предоставлен в platform-specific модулях
        
        // Предоставляем базовую реализацию логгера (будет переопределена в platform-specific модулях)
        single<Logger> {
            LoggerImpl(
                config = get<LoggingConfig>(),
                fileAppender = get<FileAppender>(),
                consoleAppender = get<ConsoleAppender>(),
                networkAppender = get<NetworkAppender>()
            )
        }
    }
