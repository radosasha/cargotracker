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

/**
 * Миграция базы данных с версии 3 на версию 4
 * Добавляет колонку note в таблицу stops
 */
val MIGRATION_3_4 = Migration(3, 4) { database ->
    database.execSQL("ALTER TABLE stops ADD COLUMN note TEXT")
}

