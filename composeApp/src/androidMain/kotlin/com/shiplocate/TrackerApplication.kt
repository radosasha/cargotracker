package com.shiplocate

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.di.AndroidKoinApp
import com.shiplocate.domain.usecase.ManageFirebaseTokensUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


/**
 * Application класс для инициализации Koin и Firebase Token Service
 */
class TrackerApplication : Application(), KoinComponent {
    private val manageFirebaseTokensUseCase: ManageFirebaseTokensUseCase by inject()
    private val logger: Logger by inject()

    // Application-scoped CoroutineScope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Инициализируем Application Context Provider
        ApplicationContextProvider.init(this)

        // Инициализируем Koin с Application-scoped зависимостями
        AndroidKoinApp.initApplicationScope(this, logger)

        // Запускаем управление Firebase токенами
        // Используем Application-scoped CoroutineScope
        applicationScope.launch {
            manageFirebaseTokensUseCase.startManaging()
        }

        if (BuildConfig.DEBUG) {
//            enableStrictMode()
        }

        logger.info(LogCategory.GENERAL, "TrackerApplication: Application initialized with Firebase Token Management")
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectContentUriWithoutPermission()
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        detectIncorrectContextUse()
                            .detectNonSdkApiUsage()
                    } else this
                }
                .detectActivityLeaks()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        // Отменяем все корутины при завершении приложения
        applicationScope.cancel()
        logger.info(LogCategory.GENERAL, "TrackerApplication: Application scope cancelled")
    }
}
