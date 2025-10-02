package com.tracker.data.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.tracker.data.database.TrackerDatabase
import platform.Foundation.NSHomeDirectory

/**
 * iOS реализация билдера Room Database
 */
actual fun getRoomDatabaseBuilder(): RoomDatabase.Builder<TrackerDatabase> {
    val dbFilePath = NSHomeDirectory() + "/${TrackerDatabase.DATABASE_NAME}"
    return Room.databaseBuilder<TrackerDatabase>(
        name = dbFilePath
    )
}

