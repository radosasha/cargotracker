package com.tracker.core.database

import android.content.Context
import androidx.room.Room

/**
 * Android реализация DatabaseProvider
 * Получает Context через конструктор
 */
actual class DatabaseProvider(private val context: Context) {
    actual fun createDatabase(databaseName: String): TrackerDatabase {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(databaseName)
        return Room.databaseBuilder<TrackerDatabase>(
            context = appContext,
            name = dbFile.absolutePath,
        ).build()
    }
}
