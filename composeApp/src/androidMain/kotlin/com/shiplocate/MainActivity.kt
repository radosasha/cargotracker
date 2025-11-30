package com.shiplocate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.di.AndroidKoinApp
import com.shiplocate.domain.model.notification.NotificationPayloadKeys
import com.shiplocate.domain.model.notification.NotificationType
import com.shiplocate.domain.usecase.GetActiveLoadUseCase
import com.shiplocate.domain.usecase.NotifyPermissionGrantedUseCase
import com.shiplocate.domain.usecase.auth.HasAuthSessionUseCase
import com.shiplocate.presentation.navigation.NavigationEvent
import com.shiplocate.presentation.navigation.NavigationEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {
    private val logger: Logger by inject()
    private val notifyPermissionGrantedUseCase: NotifyPermissionGrantedUseCase by inject()
    private val hasAuthSessionUseCase: HasAuthSessionUseCase by inject()
    private val getActiveLoadUseCase: GetActiveLoadUseCase by inject()

    // Coroutine scope для вызова suspend функций
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        logger.info(LogCategory.GENERAL, "MainActivity.onCreate() called")

        // Инициализируем Activity scope
        AndroidKoinApp.initActivityScope(logger)

        // Инициализируем Activity context
        AndroidKoinApp.initActivityContext(this, logger)

        // CrashHandler уже инициализирован через DI
        logger.info(LogCategory.GENERAL, "MainActivity initialized")
        logger.debug(LogCategory.GENERAL, "Testing debug logging")
        logger.warn(LogCategory.GENERAL, "Testing warning logging")
        logger.error(LogCategory.GENERAL, "Testing error logging")

        logger.info(LogCategory.GENERAL, "MainActivity initialization completed")

        setContent {
            App()
        }

        handleNotificationIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Проверяем разрешения при возврате в приложение
        // Это поможет обновить UI если пользователь предоставил разрешения в настройках
        // В реальном приложении здесь можно добавить логику для обновления UI
        scope.launch {
            notifyPermissionGrantedUseCase()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Уведомляем о том, что разрешения были получены через Use Case
        scope.launch {
            try {
                logger.debug(
                    LogCategory.PERMISSIONS,
                    "Permission result: requestCode=$requestCode, permissions=${permissions.joinToString()}, grantResults=${grantResults.joinToString()}"
                )
                val androidPermissionManagerImpl: AndroidPermissionManagerImpl by inject()
                androidPermissionManagerImpl.handlePermissionResult(requestCode, grantResults)
                notifyPermissionGrantedUseCase()
            } catch (e: Exception) {
                logger.error(
                    LogCategory.PERMISSIONS,
                    "Error notifying permission granted: ${e.message}",
                    e
                )
            }
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) {
            return
        }

        val extras = intent.extras
        val typeValue = extras?.get(NotificationPayloadKeys.TYPE)?.toString()
                ?: intent.getStringExtra(NotificationPayloadKeys.TYPE)
        val notificationType = typeValue?.toIntOrNull()
        if (notificationType != NotificationType.DISPATCH_MESSAGE) {
            return
        }

        val loadIdValue =
            extras?.get(NotificationPayloadKeys.LOAD_ID)?.toString()
                ?: intent.getStringExtra(NotificationPayloadKeys.LOAD_ID)
        val payloadLoadId = loadIdValue?.toLongOrNull()

        // Clear extras so we don't handle twice
        intent.removeExtra(NotificationPayloadKeys.TYPE)
        intent.removeExtra(NotificationPayloadKeys.LOAD_ID)

        scope.launch(Dispatchers.Default) {
            try {
                val hasSession = hasAuthSessionUseCase()
                if (!hasSession) {
                    logger.warn(
                        LogCategory.GENERAL,
                        "Dispatch message notification tapped, but user is not authenticated",
                    )
                    return@launch
                }

                val activeLoad = getActiveLoadUseCase()
                if (activeLoad == null) {
                    logger.warn(
                        LogCategory.GENERAL,
                        "Dispatch message notification tapped, but no active load found",
                    )
                    return@launch
                }

                val targetLoadId =
                    if (payloadLoadId != null && payloadLoadId == activeLoad.id) {
                        payloadLoadId
                    } else {
                        activeLoad.id
                    }

                logger.info(
                    LogCategory.GENERAL,
                    "Opening Messages screen from notification for loadId=$targetLoadId",
                )
                NavigationEventBus.publish(NavigationEvent.OpenMessages(targetLoadId))
            } catch (e: Exception) {
                logger.error(
                    LogCategory.GENERAL,
                    "Failed to handle dispatch message notification intent: ${e.message}",
                    e,
                )
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_GPS) {
            if(resultCode == RESULT_OK) {
                scope.launch(Dispatchers.Default) {
                    delay(1000)
                    notifyPermissionGrantedUseCase()
                }
            }
        }
    }

    companion object {
        const val REQUEST_NOTIFICATIONS_PERMISSION = 2310
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002
        const val ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE = 1003
        const val REQUEST_ENABLE_GPS = 1004
    }

    override fun onDestroy() {
        super.onDestroy()
        // Очищаем Activity context при уничтожении Activity
        AndroidKoinApp.clearActivityContext()
    }
}

@Suppress("FunctionName")
@Preview
@Composable
fun appAndroidPreview() {
    App()
}
