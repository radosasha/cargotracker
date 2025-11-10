package com.shiplocate.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shiplocate.core.database.entity.EnterStopQueueEntity

/**
 * DAO for EnterStopQueue database operations
 */
@Dao
interface EnterStopQueueDao {
    /**
     * Get all queued stop IDs
     */
    @Query("SELECT * FROM enter_stop_queue ORDER BY id ASC")
    suspend fun getAllQueuedStops(): List<EnterStopQueueEntity>

    /**
     * Check if stop ID already exists in queue
     */
    @Query("SELECT * FROM enter_stop_queue WHERE stopId = :stopId LIMIT 1")
    suspend fun getByStopId(stopId: Long): EnterStopQueueEntity?

    /**
     * Insert stop ID to queue (ignore if already exists)
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStopId(queueEntity: EnterStopQueueEntity)

    /**
     * Delete stop IDs from queue
     */
    @Query("DELETE FROM enter_stop_queue WHERE stopId IN (:stopIds)")
    suspend fun deleteByStopIds(stopIds: List<Long>)

    /**
     * Delete all queued stops
     */
    @Query("DELETE FROM enter_stop_queue")
    suspend fun deleteAll()
}

