package com.shiplocate.data.datasource

import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс для работы с настройками приложения
 * Использует DataStore для хранения данных
 */
interface PrefsDataSource {
    /**
     * Сохраняет строковое значение
     */
    suspend fun saveString(
        key: String,
        value: String,
    )

    /**
     * Получает строковое значение
     */
    suspend fun getString(key: String): String?

    /**
     * Получает поток строкового значения
     */
    fun getStringFlow(key: String): Flow<String?>

    /**
     * Сохраняет булево значение
     */
    suspend fun saveBoolean(
        key: String,
        value: Boolean,
    )

    /**
     * Получает булево значение
     */
    suspend fun getBoolean(key: String): Boolean?

    /**
     * Получает поток булева значения
     */
    fun getBooleanFlow(key: String): Flow<Boolean?>

    /**
     * Сохраняет целое число
     */
    suspend fun saveInt(
        key: String,
        value: Int,
    )

    /**
     * Получает целое число
     */
    suspend fun getInt(key: String): Int?

    /**
     * Получает поток целого числа
     */
    fun getIntFlow(key: String): Flow<Int?>

    /**
     * Удаляет значение по ключу
     */
    suspend fun remove(key: String)

    /**
     * Очищает все настройки
     */
    suspend fun clear()
}
