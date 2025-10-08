package com.tracker.core.database

/**
 * Expect класс для создания Room Database в KMP
 * Каждая платформа должна предоставить actual реализацию
 */
expect class DatabaseProvider {
    fun createDatabase(databaseName: String): TrackerDatabase
}
