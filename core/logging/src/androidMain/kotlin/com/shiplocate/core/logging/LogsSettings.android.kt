package com.shiplocate.core.logging

import android.content.Context
import java.io.File

actual class LogsSettings(private val context: Context) {

    private val logDirectory: File by lazy {
        val dir = File(context.filesDir, "logs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    actual fun getLogsDirectory(): String {
        return logDirectory.absolutePath
    }
}
