package com.shiplocate.data.datasource.route

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Encapsulates DataStore operations for Route preferences
 */
class RoutePreferences(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val KEY_LOAD_ID = longPreferencesKey("route_load_id")
        private val KEY_ROUTE_JSON = stringPreferencesKey("route_json")
        private val KEY_PROVIDER = stringPreferencesKey("route_provider")
        private val KEY_REQUIRE_UPDATE = booleanPreferencesKey("route_require_update")
    }

    suspend fun saveLoadId(loadId: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LOAD_ID] = loadId
        }
    }

    suspend fun getLoadId(): Long? {
        return dataStore.data.first()[KEY_LOAD_ID]
    }

    suspend fun saveRouteJson(routeJson: String) {
        dataStore.edit { preferences ->
            preferences[KEY_ROUTE_JSON] = routeJson
        }
    }

    suspend fun getRouteJson(): String? {
        return dataStore.data.first()[KEY_ROUTE_JSON]
    }

    suspend fun saveProvider(provider: String) {
        dataStore.edit { preferences ->
            preferences[KEY_PROVIDER] = provider
        }
    }

    suspend fun getProvider(): String? {
        return dataStore.data.first()[KEY_PROVIDER]
    }

    suspend fun saveRequireUpdate(requireUpdate: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_REQUIRE_UPDATE] = requireUpdate
        }
    }

    suspend fun getRequireUpdate(): Boolean {
        return dataStore.data.first()[KEY_REQUIRE_UPDATE] ?: true
    }

    suspend fun saveRoute(
        loadId: Long,
        routeJson: String,
        provider: String,
        requireUpdate: Boolean = true,
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_LOAD_ID] = loadId
            preferences[KEY_ROUTE_JSON] = routeJson
            preferences[KEY_PROVIDER] = provider
            preferences[KEY_REQUIRE_UPDATE] = requireUpdate
        }
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_LOAD_ID)
            preferences.remove(KEY_ROUTE_JSON)
            preferences.remove(KEY_PROVIDER)
            preferences.remove(KEY_REQUIRE_UPDATE)
        }
    }

    fun observeRouteJson(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[KEY_ROUTE_JSON]
        }
    }
}

