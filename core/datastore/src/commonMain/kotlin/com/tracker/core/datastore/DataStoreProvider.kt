package com.tracker.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Expect класс для создания DataStore в KMP
 * Каждая платформа должна предоставить actual реализацию
 */
expect class DataStoreProvider() {
    fun createDataStore(): DataStore<Preferences>
}
