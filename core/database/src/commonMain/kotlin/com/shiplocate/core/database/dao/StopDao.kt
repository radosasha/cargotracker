package com.shiplocate.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.shiplocate.core.database.entity.StopEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Stop database operations
 */
@Dao
interface StopDao {
    @Query("SELECT * FROM stops WHERE loadId = :loadId ORDER BY stopIndex ASC")
    suspend fun getStopsByLoadId(loadId: Long): List<StopEntity>

    @Query("SELECT * FROM stops WHERE loadId = :loadId AND enter = 0 ORDER BY stopIndex ASC")
    suspend fun getNotEnteredStopsByLoad(loadId: Long): List<StopEntity>

    @Query("SELECT * FROM stops")
    suspend fun getAllStops(): List<StopEntity>

    @Insert
    suspend fun insertStops(stops: List<StopEntity>)

    @Update
    suspend fun updateStops(stops: List<StopEntity>)

    @Query("DELETE FROM stops WHERE loadId = :loadId AND serverId NOT IN (:serverIds)")
    suspend fun deleteStopsNotIn(loadId: Long, serverIds: List<Long>)

    @Query("DELETE FROM stops WHERE loadId = :loadId")
    suspend fun deleteStopsByLoadId(loadId: Long)

    @Query("DELETE FROM stops")
    suspend fun deleteAll()

    /**
     * Observe stops where enter == 0
     * Returns Flow that emits list of StopEntity when stops with enter=0 change
     */
    @Query("SELECT * FROM stops WHERE enter = 0 ORDER BY stopIndex ASC")
    fun observeNotEnteredStops(): Flow<List<StopEntity>>
}

