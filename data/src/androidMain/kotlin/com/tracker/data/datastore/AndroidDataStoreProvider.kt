package com.tracker.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val dataStoreFileName = "tracker.preferences_pb"

/**
 * Android actual реализация DataStoreProvider
 * Получает Context через Koin DI
 */
actual class DataStoreProvider : KoinComponent {

    private val context: Context by inject()

    actual fun createDataStore(): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = { 
                context.filesDir.resolve(dataStoreFileName).absolutePath.toPath()
            },
            corruptionHandler = null,
            migrations = emptyList(),
            scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        )
    }
}
