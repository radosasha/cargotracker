package com.shiplocate.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.shiplocate.core.database.entity.LoadEntity

/**
 * DAO for Load database operations
 */
@Dao
interface LoadDao {
    @Query("SELECT * FROM loads ORDER BY createdAt DESC")
    suspend fun getAllLoads(): List<LoadEntity>

    @Query("SELECT * FROM loads WHERE id = :loadId")
    suspend fun getLoadById(loadId: Long): LoadEntity?

    @Query("SELECT * FROM loads WHERE serverId = :serverId")
    suspend fun getLoadByServerId(serverId: Long): LoadEntity?

    @Insert
    suspend fun insertLoads(loads: List<LoadEntity>)

    @Update
    suspend fun updateLoads(loads: List<LoadEntity>)

    @Query("DELETE FROM loads WHERE serverId NOT IN (:serverIds)")
    suspend fun deleteLoadsNotIn(serverIds: List<Long>)

    @Query("DELETE FROM loads")
    suspend fun deleteAll()
}
