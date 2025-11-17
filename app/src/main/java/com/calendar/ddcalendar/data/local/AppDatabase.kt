package com.calendar.ddcalendar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.calendar.ddcalendar.data.model.EventEntity

/**
 * 应用数据库
 */
@Database(
    entities = [EventEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}
