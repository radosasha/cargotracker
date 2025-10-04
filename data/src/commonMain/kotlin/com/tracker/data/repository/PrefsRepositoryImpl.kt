package com.tracker.data.repository

import com.tracker.data.datasource.PrefsDataSource
import com.tracker.domain.repository.PrefsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Реализация PrefsRepository в Data слое
 * Делегирует вызовы в PrefsDataSource
 */
class PrefsRepositoryImpl(
    private val prefsDataSource: PrefsDataSource
) : PrefsRepository {
    
    companion object {
        private const val TRACKING_STATE_KEY = "tracking_state"
        private const val GPS_ACCURACY_KEY = "gps_accuracy"
        private const val GPS_INTERVAL_KEY = "gps_interval"
        private const val DISTANCE_FILTER_KEY = "distance_filter"
    }
    
    override suspend fun saveTrackingState(isTracking: Boolean) {
        prefsDataSource.saveBoolean(TRACKING_STATE_KEY, isTracking)
    }
    
    override suspend fun getTrackingState(): Boolean? {
        return prefsDataSource.getBoolean(TRACKING_STATE_KEY)
    }
    
    override fun getTrackingStateFlow(): Flow<Boolean?> {
        return prefsDataSource.getBooleanFlow(TRACKING_STATE_KEY)
    }
    
    override suspend fun saveGpsAccuracy(accuracy: String) {
        prefsDataSource.saveString(GPS_ACCURACY_KEY, accuracy)
    }
    
    override suspend fun getGpsAccuracy(): String? {
        return prefsDataSource.getString(GPS_ACCURACY_KEY)
    }
    
    override fun getGpsAccuracyFlow(): Flow<String?> {
        return prefsDataSource.getStringFlow(GPS_ACCURACY_KEY)
    }
    
    override suspend fun saveGpsInterval(interval: Int) {
        prefsDataSource.saveInt(GPS_INTERVAL_KEY, interval)
    }
    
    override suspend fun getGpsInterval(): Int? {
        return prefsDataSource.getInt(GPS_INTERVAL_KEY)
    }
    
    override fun getGpsIntervalFlow(): Flow<Int?> {
        return prefsDataSource.getIntFlow(GPS_INTERVAL_KEY)
    }
    
    override suspend fun saveDistanceFilter(distance: Int) {
        prefsDataSource.saveInt(DISTANCE_FILTER_KEY, distance)
    }
    
    override suspend fun getDistanceFilter(): Int? {
        return prefsDataSource.getInt(DISTANCE_FILTER_KEY)
    }
    
    override fun getDistanceFilterFlow(): Flow<Int?> {
        return prefsDataSource.getIntFlow(DISTANCE_FILTER_KEY)
    }
    
    override suspend fun clearAllSettings() {
        prefsDataSource.clear()
    }
}
