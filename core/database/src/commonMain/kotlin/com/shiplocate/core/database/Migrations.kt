package com.shiplocate.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Миграция базы данных с версии 2 на версию 3
 * Добавляет колонку locationName в таблицу stops
 */
val MIGRATION_2_3 = Migration(2, 3) { database ->
    database.execSQL("ALTER TABLE stops ADD COLUMN locationName TEXT NOT NULL DEFAULT ''")
}

