package com.shiplocate.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shiplocate.core.database.entity.StopEntity

/**
 * DAO for Stop database operations
 */
@Dao
interface StopDao {
    @Query("SELECT * FROM stops WHERE loadId = :loadId ORDER BY stopIndex ASC")
    suspend fun getStopsByLoadId(loadId: Long): List<StopEntity>

    @Query("SELECT * FROM stops")
    suspend fun getAllStops(): List<StopEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<StopEntity>)

    @Query("DELETE FROM stops")
    suspend fun deleteAll()
}

