package com.tracker.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tracker.data.database.TrackerDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android реализация билдера Room Database
 */
actual fun getRoomDatabaseBuilder(): RoomDatabase.Builder<TrackerDatabase> {
    val koinComponent = object : KoinComponent {
        val context: Context by inject()
    }
    val appContext = koinComponent.context.applicationContext
    val dbFile = appContext.getDatabasePath(TrackerDatabase.DATABASE_NAME)
    return Room.databaseBuilder<TrackerDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

