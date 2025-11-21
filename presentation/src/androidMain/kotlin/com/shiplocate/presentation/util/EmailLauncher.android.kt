package com.shiplocate.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android реализация для открытия email клиента
 */
actual class EmailLauncher(private val context: Context) {
    actual fun openEmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Send email")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Если не удалось открыть email клиент, пытаемся открыть через ACTION_SEND
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(intent, "Send email")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e2: Exception) {
                // Если и это не сработало, просто игнорируем ошибку
            }
        }
    }
}

@Composable
actual fun rememberEmailLauncher(): EmailLauncher {
    val context = LocalContext.current
    return remember(context) { EmailLauncher(context) }
}

