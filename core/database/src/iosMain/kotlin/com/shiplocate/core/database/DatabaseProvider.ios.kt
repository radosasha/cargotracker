package com.shiplocate.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS реализация DatabaseProvider
 * Не требует Context, использует NSDocumentDirectory
 */
actual class DatabaseProvider() {

    @OptIn(ExperimentalForeignApi::class)
    actual fun createDatabase(databaseName: String): TrackerDatabase {
        val dbFile = NSURL.fileURLWithPath(
            path = "${NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null
            )!!.path}/$databaseName"
        )
        return Room.databaseBuilder<TrackerDatabase>(
            name = dbFile.path!!
        )
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(dropAllTables = false)
        .build()
    }
}