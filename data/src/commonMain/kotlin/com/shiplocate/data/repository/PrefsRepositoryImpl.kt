package com.shiplocate.data.repository

import com.shiplocate.data.datasource.PrefsDataSource
import com.shiplocate.domain.repository.PrefsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Реализация PrefsRepository в Data слое
 * Делегирует вызовы в PrefsDataSource
 */
class PrefsRepositoryImpl(
    private val prefsDataSource: PrefsDataSource,
) : PrefsRepository {
    companion object {
        private const val GPS_ACCURACY_KEY = "gps_accuracy"
        private const val GPS_INTERVAL_KEY = "gps_interval"
        private const val DISTANCE_FILTER_KEY = "distance_filter"
        private const val PHONE_KEY = "phone_number"
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

    override suspend fun savePhoneNumber(phoneNumber: String) {
        prefsDataSource.saveString(PHONE_KEY, phoneNumber)
    }

    override suspend fun getPhoneNumber(): String? {
        return prefsDataSource.getString(PHONE_KEY)
    }
}
