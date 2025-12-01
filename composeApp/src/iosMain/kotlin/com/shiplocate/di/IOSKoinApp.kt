@file:OptIn(kotlin.experimental.ExperimentalObjCName::class)

package com.shiplocate.di

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.notification.NotificationPayloadKeys
import com.shiplocate.domain.repository.NotificationRepository
import com.shiplocate.domain.usecase.HandlePushNotificationWhenAppKilledUseCase
import com.shiplocate.domain.usecase.ManageFirebaseTokensUseCase
import com.shiplocate.domain.usecase.auth.HasAuthSessionUseCase
import com.shiplocate.trackingsdk.di.trackingSDKModule
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
    private var notificationRepository: NotificationRepository? = null
    private var handlePushNotificationWhenAppKilledUseCase: HandlePushNotificationWhenAppKilledUseCase? = null
    private var hasAuthSessionUseCase: HasAuthSessionUseCase? = null
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
            modules(appModule + iosModule + iosPlatformModule + iosAppModule + trackingSDKModule)
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
        notificationRepository: NotificationRepository,
        handlePushNotificationWhenAppKilledUseCase: HandlePushNotificationWhenAppKilledUseCase,
        hasAuthSessionUseCase: HasAuthSessionUseCase,
        logger: Logger,
    ) {
        this.manageFirebaseTokensUseCase = manageFirebaseTokensUseCase
        this.notificationRepository = notificationRepository
        this.handlePushNotificationWhenAppKilledUseCase = handlePushNotificationWhenAppKilledUseCase
        this.hasAuthSessionUseCase = hasAuthSessionUseCase
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

        val repository = notificationRepository
        if (repository == null) {
            logger?.warn(LogCategory.GENERAL, "IOSKoinApp: NotificationRepository not set, skipping")
            return
        }

        try {
            scope.launch {
                repository.onNewTokenReceived(token)
            }
            logger?.info(LogCategory.GENERAL, "IOSKoinApp: Token passed to repository successfully")
        } catch (e: Exception) {
            logger?.error(LogCategory.GENERAL, "IOSKoinApp: Failed to pass token: ${e.message}")
        }
    }

    /**
     * Получение текущего Firebase токена от iOS
     * Сохраняет токен в локальное хранилище
     */
    fun onCurrentTokenReceived(token: String?) {
        if (!isInitialized) {
            logger?.warn(LogCategory.GENERAL, "IOSKoinApp: Not initialized, skipping current token")
            return
        }

        val repository = notificationRepository
        if (repository == null) {
            logger?.warn(LogCategory.GENERAL, "IOSKoinApp: NotificationRepository not set, skipping")
            return
        }

        try {
            if (token != null) {
                scope.launch {
                    repository.saveToken(token)
                }
                logger?.info(LogCategory.GENERAL, "IOSKoinApp: Current token saved to repository successfully")
            }
        } catch (e: Exception) {
            logger?.error(LogCategory.GENERAL, "IOSKoinApp: Failed to save current token: ${e.message}")
        }
    }

    /**
     * Обработка push-уведомления когда приложение запущено
     * Вызывается из iOS кода
     */
    fun onPushNotificationReceived(payload: Map<String, String>? = null) {
        if (!isInitialized) {
            logger?.warn(LogCategory.GENERAL, "IOSKoinApp: Not initialized, skipping push notification")
            return
        }

        scope.launch {
            if (hasAuthSessionUseCase?.invoke() == false) {
                logger?.warn(LogCategory.GENERAL, "IOSKoinApp: Ignore new push, not authorized")
                return@launch
            }

            val repository = notificationRepository
            if (repository == null) {
                logger?.warn(LogCategory.GENERAL, "IOSKoinApp: NotificationRepository not set, skipping push notification")
                return@launch
            }

            // Уведомляем о получении push (для случая когда приложение запущено)
            val type = payload?.get(NotificationPayloadKeys.TYPE)?.toIntOrNull()
            repository.pushReceived(type)

            // Обрабатываем push когда приложение не запущено
            val handlePushUseCase = handlePushNotificationWhenAppKilledUseCase
            if (handlePushUseCase != null) {
                handlePushUseCase()
            } else {
                logger?.warn(LogCategory.GENERAL, "IOSKoinApp: HandlePushNotificationWhenAppKilledUseCase not set, skipping")
            }

            logger?.info(LogCategory.GENERAL, "IOSKoinApp: Push notification processed successfully")
        }
    }

    /**
     * Остановка Koin
     */
    fun stop() {
        applicationScope?.cancel()
        applicationScope = null
        manageFirebaseTokensUseCase = null
        notificationRepository = null
        handlePushNotificationWhenAppKilledUseCase = null
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

fun handleIOSPushNotification() {
    IOSKoinApp.onPushNotificationReceived(null)
}

fun handleIOSPushNotification(payload: Map<String, String>) {
    IOSKoinApp.onPushNotificationReceived(payload)
}
