package com.tracker.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.tracker.data.database.dao.LocationDao
import com.tracker.data.database.entity.LocationEntity

/**
 * Room Database для хранения GPS координат
 */
@Database(
    entities = [LocationEntity::class],
    version = 1,
    exportSchema = true
)
@ConstructedBy(TrackerDatabaseConstructor::class)
abstract class TrackerDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    
    companion object {
        const val DATABASE_NAME = "tracker.db"
    }
}

/**
 * Constructor для Room Database
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object TrackerDatabaseConstructor : RoomDatabaseConstructor<TrackerDatabase> {
    override fun initialize(): TrackerDatabase
}


