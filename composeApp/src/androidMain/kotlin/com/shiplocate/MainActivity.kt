package com.shiplocate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.di.AndroidKoinApp
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {
    private val logger: Logger by inject()
    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            // Обработка результатов запроса разрешений
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                // Все разрешения предоставлены, можно продолжить
                logger.info(LogCategory.PERMISSIONS, "All permissions granted")
            } else {
                // Некоторые разрешения отклонены
                logger.warn(LogCategory.PERMISSIONS, "Some permissions denied: $permissions")
            }
        }

    private val backgroundLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                logger.info(LogCategory.PERMISSIONS, "Background location permission granted")
            } else {
                logger.warn(LogCategory.PERMISSIONS, "Background location permission denied")
            }
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                logger.info(LogCategory.PERMISSIONS, "Notification permission granted")
            } else {
                logger.warn(LogCategory.PERMISSIONS, "Notification permission denied")
            }
        }

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
    }

    override fun onResume() {
        super.onResume()
        // Проверяем разрешения при возврате в приложение
        // Это поможет обновить UI если пользователь предоставил разрешения в настройках
        // В реальном приложении здесь можно добавить логику для обновления UI
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Передаем результат в AndroidPermissionRequester для обработки
        try {
            val permissionRequester = AndroidPermissionRequester(this)
            permissionRequester.handlePermissionResult(requestCode, grantResults)
        } catch (e: Exception) {
            logger.debug(LogCategory.PERMISSIONS, "Permission result: requestCode=$requestCode, permissions=${permissions.joinToString()}, grantResults=${grantResults.joinToString()}")
        }
    }

    companion object {
        const val REQUEST_ALL_PERMISSIONS = 2311
        const val REQUEST_NOTIFICATIONS_PERMISSION = 2310
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002
        const val ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE = 1003
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
