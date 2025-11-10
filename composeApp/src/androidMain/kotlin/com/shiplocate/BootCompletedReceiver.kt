package com.shiplocate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.usecase.StartTrackingUseCase
import com.shiplocate.domain.usecase.auth.HasAuthSessionUseCase
import com.shiplocate.domain.usecase.load.GetConnectedLoadUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootCompletedReceiver : BroadcastReceiver(), KoinComponent {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val logger: Logger by inject()
    private val hasAuthSessionUseCase: HasAuthSessionUseCase by inject()
    private val getConnectedLoadUseCase: GetConnectedLoadUseCase by inject()
    private val startTrackingUseCase: StartTrackingUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        // Логируем в самое начало для отладки
        logger.info(LogCategory.LOCATION, "BootCompletedReceiver: onReceive called with action: ${intent.action}")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            logger.info(LogCategory.LOCATION, "BootCompletedReceiver: Device rebooted, checking conditions to restart tracking")

            // Используем goAsync() для продолжения работы после onReceive
            val pendingResult = goAsync()

            // Получаем WakeLock для удержания устройства проснувшимся
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "BootCompletedReceiver::WakeLock",
            ).apply {
                acquire(10_000L) // 10 секунд должно быть достаточно
            }

            scope.launch {
                try {
                    // 1. Проверяем авторизацию
                    val isAuthenticated = hasAuthSessionUseCase()
                    if (!isAuthenticated) {
                        logger.info(LogCategory.LOCATION, "BootCompletedReceiver: User not authenticated, skipping tracking restart")
                        return@launch
                    }

                    logger.info(LogCategory.LOCATION, "BootCompletedReceiver: User is authenticated, checking for connected load")

                    // 2. Проверяем наличие connected load (сначала кеш, потом сервер)
                    val connectedLoad = getConnectedLoadUseCase()
                    if (connectedLoad == null) {
                        logger.info(LogCategory.LOCATION, "BootCompletedReceiver: No connected load found, skipping tracking restart")
                        return@launch
                    }

                    logger.info(LogCategory.LOCATION, "BootCompletedReceiver: Connected load found, starting tracking")

                    // 3. Запускаем трекинг (StartTrackingUseCase проверит разрешения)
                    val result = startTrackingUseCase(connectedLoad.id)

                    if (result.isSuccess) {
                        logger.info(LogCategory.LOCATION, "BootCompletedReceiver: Tracking restarted successfully after reboot")
                    } else {
                        logger.warn(
                            LogCategory.LOCATION,
                            "BootCompletedReceiver: Failed to restart tracking: ${result.exceptionOrNull()?.message}",
                        )
                    }
                } catch (e: Exception) {
                    logger.error(LogCategory.LOCATION, "BootCompletedReceiver: Error restarting tracking after reboot", e)
                } finally {
                    // Освобождаем WakeLock
                    if (wakeLock.isHeld) {
                        wakeLock.release()
                    }
                    // Завершаем работу BroadcastReceiver
                    pendingResult.finish()
                }
            }
        }
    }
}

