package com.tracker.data.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.tracker.data.database.TrackerDatabase
import com.tracker.data.database.TrackerDatabaseConstructor
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS реализация билдера Room Database
 */
@OptIn(ExperimentalForeignApi::class)
actual fun getRoomDatabaseBuilder(): RoomDatabase.Builder<TrackerDatabase> {
    val dbFile = NSURL.fileURLWithPath(
        path = "${NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )!!.path}/${TrackerDatabase.DATABASE_NAME}"
    )
    return Room.databaseBuilder<TrackerDatabase>(
        name = dbFile.path!!
    )
}

