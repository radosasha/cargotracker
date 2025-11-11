package com.shiplocate.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.shiplocate.core.database.dao.EnterStopQueueDao
import com.shiplocate.core.database.dao.LoadDao
import com.shiplocate.core.database.dao.LocationDao
import com.shiplocate.core.database.dao.StopDao
import com.shiplocate.core.database.entity.EnterStopQueueEntity
import com.shiplocate.core.database.entity.LoadEntity
import com.shiplocate.core.database.entity.LocationEntity
import com.shiplocate.core.database.entity.StopEntity

/**
 * Room Database для хранения GPS координат и loads
 */
@Database(
    entities = [LocationEntity::class, LoadEntity::class, StopEntity::class, EnterStopQueueEntity::class],
    version = 4,
    exportSchema = true,
)
@ConstructedBy(TrackerDatabaseConstructor::class)
abstract class TrackerDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    abstract fun loadDao(): LoadDao

    abstract fun stopDao(): StopDao

    abstract fun enterStopQueueDao(): EnterStopQueueDao

    companion object {
        const val DATABASE_NAME = "shiplocate.db"
    }
}

/**
 * Constructor для Room Database
 * KSP автоматически генерирует actual реализацию
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object TrackerDatabaseConstructor : RoomDatabaseConstructor<TrackerDatabase> {
    override fun initialize(): TrackerDatabase
}
