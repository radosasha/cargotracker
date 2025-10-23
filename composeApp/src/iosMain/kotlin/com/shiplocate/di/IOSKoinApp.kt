@file:OptIn(kotlin.experimental.ExperimentalObjCName::class)

package com.shiplocate.di

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.FirebaseTokenServiceDataSource
import com.shiplocate.domain.usecase.ManageFirebaseTokensUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/**
 * iOS инициализация Koin DI контейнера
 *
 * Следует принципам Clean Architecture и SOLID:
 * - Не использует GlobalContext (плохая практика)
 * - Внедряет зависимости через конструкторы
 */
object IOSKoinApp {
    private var isInitialized = false
    private var applicationScope: CoroutineScope? = null

    // Зависимости внедряются через конструкторы (Clean Architecture)
    private var manageFirebaseTokensUseCase: ManageFirebaseTokensUseCase? = null
    private var firebaseTokenServiceDataSource: FirebaseTokenServiceDataSource? = null
    private var logger: Logger? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Инициализация Application-scoped зависимостей
     * Вызывается при запуске приложения
     */
    fun initApplicationScope() {
        if (isInitialized) {
            logger?.info(LogCategory.GENERAL, "IOSKoinApp: Already initialized, skipping")
            return
        }

        logger?.info(LogCategory.GENERAL, "IOSKoinApp: Starting Koin initialization...")

        startKoin {
            printLogger()
            modules(appModule + iosModule + iosPlatformModule + iosAppModule)
        }

        applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        isInitialized = true

        logger?.info(LogCategory.GENERAL, "IOSKoinApp: Application scope initialized successfully")
    }

    /**
     * Установка зависимостей (Clean Architecture подход)
     * Вызывается после инициализации Koin
     */
    fun setDependencies(
        manageFirebaseTokensUseCase: ManageFirebaseTokensUseCase,
        firebaseTokenServiceDataSource: FirebaseTokenServiceDataSource,
        logger: Logger,
    ) {
        this.manageFirebaseTokensUseCase = manageFirebaseTokensUseCase
        this.firebaseTokenServiceDataSource = firebaseTokenServiceDataSource
        this.logger = logger
        logger.info(LogCategory.GENERAL, "IOSKoinApp: Dependencies set successfully")
    }

    /**
     * Запустить управление Firebase токенами
     */

    fun startFirebaseTokenService() {
        if (!isInitialized) {
            logger?.warn(LogCategory.GENERAL, "IOSKoinApp: Not initialized, skipping token service start")
            return
        }

        val useCase = manageFirebaseTokensUseCase
        if (useCase == null) {
            logger?.warn(LogCategory.GENERAL, "IOSKoinApp: ManageFirebaseTokensUseCase not set, skipping")
            return
        }

        try {
            applicationScope?.launch {
                useCase.startManaging()
            }
            logger?.info(LogCategory.GENERAL, "IOSKoinApp: Firebase Token Management started successfully")
        } catch (e: Exception) {
            logger?.error(LogCategory.GENERAL, "IOSKoinApp: Failed to start Firebase Token Management: ${e.message}")
        }
    }

    /**
     * Получение нового Firebase токена от iOS
     */
    fun onNewTokenReceived(token: String) {
        if (!isInitialized) {
            logger?.warn(LogCategory.GENERAL, "IOSKoinApp: Not initialized, caching token for later")
            return
        }

        val dataSource = firebaseTokenServiceDataSource
        if (dataSource == null) {
            logger?.warn(LogCategory.GENERAL, "IOSKoinApp: FirebaseTokenServiceDataSource not set, skipping")
            return
        }

        try {
            scope.launch {
                dataSource.onNewTokenReceived(token)
            }
            logger?.info(LogCategory.GENERAL, "IOSKoinApp: Token passed to data source successfully")
        } catch (e: Exception) {
            logger?.error(LogCategory.GENERAL, "IOSKoinApp: Failed to pass token: ${e.message}")
        }
    }

    /**
     * Получение текущего Firebase токена от iOS
     */
    fun onCurrentTokenReceived(token: String?) {
        if (!isInitialized) {
            logger?.warn(LogCategory.GENERAL, "IOSKoinApp: Not initialized, skipping current token")
            return
        }

        val dataSource = firebaseTokenServiceDataSource
        if (dataSource == null) {
            logger?.warn(LogCategory.GENERAL, "IOSKoinApp: FirebaseTokenServiceDataSource not set, skipping")
            return
        }

        try {
            dataSource.onTokenReceived(token)
            logger?.info(LogCategory.GENERAL, "IOSKoinApp: Current token passed to data source successfully")
        } catch (e: Exception) {
            logger?.error(LogCategory.GENERAL, "IOSKoinApp: Failed to pass current token: ${e.message}")
        }
    }

    /**
     * Остановка Koin
     */
    fun stop() {
        applicationScope?.cancel()
        applicationScope = null
        manageFirebaseTokensUseCase = null
        firebaseTokenServiceDataSource = null
        stopKoin()
        isInitialized = false
        logger?.info(LogCategory.GENERAL, "IOSKoinApp: Stopped successfully")
    }
}

/**
 * Top-level функции для доступа из Swift
 * Kotlin objects не всегда корректно экспортируются в Swift,
 * поэтому используем top-level функции как обертки
 */
fun initIOSKoinApp() {
    IOSKoinApp.initApplicationScope()
}

fun initIOSKoinAppDependencies() {
    IOSKoinAppInitializer().initializeDependencies()
}

fun startIOSFirebaseTokenService() {
    IOSKoinApp.startFirebaseTokenService()
}

fun handleIOSNewToken(token: String) {
    IOSKoinApp.onNewTokenReceived(token)
}

fun handleIOSCurrentToken(token: String?) {
    IOSKoinApp.onCurrentTokenReceived(token)
}
