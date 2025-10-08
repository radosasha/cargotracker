package com.tracker.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath

/**
 * Android actual реализация DataStoreProvider
 * Получает Context через конструктор
 */
actual class DataStoreProvider(private val context: Context) {
    
    actual fun createDataStore(fileName: String): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = { 
                context.filesDir.resolve(fileName).absolutePath.toPath()
            },
            corruptionHandler = null,
            migrations = emptyList(),
            scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        )
    }
}
