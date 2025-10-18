package com.shiplocate.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shiplocate.core.database.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с GPS координатами в базе данных
 */
@Dao
interface LocationDao {
    /**
     * Вставляет координату в базу данных
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity): Long

    /**
     * Вставляет список координат
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>)

    /**
     * Получает все неотправленные координаты
     */
    @Query("SELECT * FROM locations WHERE isSent = 0 ORDER BY timestamp ASC")
    suspend fun getUnsentLocations(): List<LocationEntity>

    /**
     * Получает количество неотправленных координат
     */
    @Query("SELECT COUNT(*) FROM locations WHERE isSent = 0")
    suspend fun getUnsentCount(): Int

    /**
     * Получает последнюю сохраненную координату
     */
    @Query("SELECT * FROM locations ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLocation(): LocationEntity?

    /**
     * Получает последнюю неотправленную координату
     */
    @Query("SELECT * FROM locations WHERE isSent = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastUnsentLocation(): LocationEntity?

    /**
     * Помечает координаты как отправленные
     */
    @Query("UPDATE locations SET isSent = 1 WHERE id IN (:ids)")
    suspend fun markAsSent(ids: List<Long>)

    /**
     * Удаляет отправленные координаты
     */
    @Query("DELETE FROM locations WHERE isSent = 1")
    suspend fun deleteSentLocations()

    /**
     * Удаляет координату по ID
     */
    @Query("DELETE FROM locations WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Удаляет координаты по списку ID
     */
    @Query("DELETE FROM locations WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    /**
     * Удаляет старые координаты (старше указанного времени)
     */
    @Query("DELETE FROM locations WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)

    /**
     * Получает все координаты (для отладки)
     */
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    suspend fun getAllLocations(): List<LocationEntity>

    /**
     * Наблюдает за количеством неотправленных координат
     */
    @Query("SELECT COUNT(*) FROM locations WHERE isSent = 0")
    fun observeUnsentCount(): Flow<Int>

    /**
     * Транзакция: помечает координаты как отправленные и удаляет их
     */
    @Transaction
    suspend fun markAsSentAndDelete(ids: List<Long>) {
        markAsSent(ids)
        deleteByIds(ids)
    }
}
