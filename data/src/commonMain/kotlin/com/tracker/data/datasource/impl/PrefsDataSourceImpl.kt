package com.tracker.data.datasource.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tracker.data.datasource.PrefsDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Реализация PrefsDataSource с использованием DataStore
 */
class PrefsDataSourceImpl(
    private val dataStore: DataStore<Preferences>
) : PrefsDataSource {
    
    override suspend fun saveString(key: String, value: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }
    
    override suspend fun getString(key: String): String? {
        return dataStore.data.first()[stringPreferencesKey(key)]
    }
    
    override fun getStringFlow(key: String): Flow<String?> {
        return dataStore.data
            .catch { exception ->
                // Логируем ошибку, но не прерываем поток
                println("Error reading string preference: ${exception.message}")
            }
            .map { preferences ->
                preferences[stringPreferencesKey(key)]
            }
    }
    
    override suspend fun saveBoolean(key: String, value: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }
    
    override suspend fun getBoolean(key: String): Boolean? {
        return dataStore.data.first()[booleanPreferencesKey(key)]
    }
    
    override fun getBooleanFlow(key: String): Flow<Boolean?> {
        return dataStore.data
            .catch { exception ->
                println("Error reading boolean preference: ${exception.message}")
            }
            .map { preferences ->
                preferences[booleanPreferencesKey(key)]
            }
    }
    
    override suspend fun saveInt(key: String, value: Int) {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey(key)] = value
        }
    }
    
    override suspend fun getInt(key: String): Int? {
        return dataStore.data.first()[intPreferencesKey(key)]
    }
    
    override fun getIntFlow(key: String): Flow<Int?> {
        return dataStore.data
            .catch { exception ->
                println("Error reading int preference: ${exception.message}")
            }
            .map { preferences ->
                preferences[intPreferencesKey(key)]
            }
    }
    
    override suspend fun remove(key: String) {
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
            preferences.remove(booleanPreferencesKey(key))
            preferences.remove(intPreferencesKey(key))
        }
    }
    
    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
