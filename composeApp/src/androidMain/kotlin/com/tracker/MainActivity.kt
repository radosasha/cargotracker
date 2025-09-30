package com.tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tracker.di.AndroidKoinApp

class MainActivity : ComponentActivity() {
    
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Обработка результатов запроса разрешений
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Все разрешения предоставлены, можно продолжить
            println("All permissions granted")
        } else {
            // Некоторые разрешения отклонены
            println("Some permissions denied: $permissions")
        }
    }
    
    private val backgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("Background location permission granted")
        } else {
            println("Background location permission denied")
        }
    }
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("Notification permission granted")
        } else {
            println("Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        println("MainActivity.onCreate() called")
        
        // Инициализируем ApplicationContextProvider
        ApplicationContextProvider.init(this)
        
        // Инициализируем Koin DI
        AndroidKoinApp.init()
        
        // Инициализируем Activity scope
        AndroidKoinApp.initActivityScope(this)
        
        println("MainActivity initialization completed")

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
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        // Передаем результат в AndroidPermissionRequester для обработки
        try {
            val permissionRequester = AndroidPermissionRequester(this)
            permissionRequester.handlePermissionResult(requestCode, grantResults)
        } catch (e: Exception) {
            println("Error handling permission result: ${e.message}")
        }
    }
    
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1003
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Очищаем Activity scope при уничтожении Activity
        AndroidKoinApp.clearActivityScope()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}