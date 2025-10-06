package com.tracker.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject

/**
 * Android реализация createDatabaseBuilder
 */
actual fun createDatabaseBuilder(): RoomDatabase.Builder<TrackerDatabase> {
    val koinComponent = object : KoinComponent {
        val context: Context by inject(Context::class.java)
    }
    val appContext = koinComponent.context.applicationContext
    val dbFile = appContext.getDatabasePath(TrackerDatabase.DATABASE_NAME)
    return Room.databaseBuilder<TrackerDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
