package com.tracker.core.database

import androidx.room.RoomDatabase

/**
 * Создает Room Database Builder
 * Каждая платформа должна предоставить actual реализацию
 */
expect fun createDatabaseBuilder(): RoomDatabase.Builder<TrackerDatabase>
