package com.tracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tracker.core.database.entity.LoadEntity

/**
 * DAO for Load database operations
 */
@Dao
interface LoadDao {
    
    @Query("SELECT * FROM loads ORDER BY createdAt DESC")
    suspend fun getAllLoads(): List<LoadEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoads(loads: List<LoadEntity>)
    
    @Query("DELETE FROM loads")
    suspend fun deleteAll()
}


