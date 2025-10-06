package com.tracker.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS реализация createDatabaseBuilder
 */
@OptIn(ExperimentalForeignApi::class)
actual fun createDatabaseBuilder(): RoomDatabase.Builder<TrackerDatabase> {
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
