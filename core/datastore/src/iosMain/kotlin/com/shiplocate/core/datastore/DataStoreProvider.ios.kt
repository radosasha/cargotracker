package com.shiplocate.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS actual реализация DataStoreProvider
 * Не требует Context, использует NSDocumentDirectory
 */
actual class DataStoreProvider {
    @OptIn(ExperimentalForeignApi::class)
    actual fun createDataStore(fileName: String): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                val documentDirectory: NSURL? =
                    NSFileManager.defaultManager.URLForDirectory(
                        directory = NSDocumentDirectory,
                        inDomain = NSUserDomainMask,
                        appropriateForURL = null,
                        create = false,
                        error = null,
                    )
                (requireNotNull(documentDirectory).path + "/$fileName").toPath()
            },
            corruptionHandler = null,
            migrations = emptyList(),
            scope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
        )
    }
}
