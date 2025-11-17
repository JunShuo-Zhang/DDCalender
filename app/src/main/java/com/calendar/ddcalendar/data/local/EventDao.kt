package com.calendar.ddcalendar.data.local

import androidx.room.*
import com.calendar.ddcalendar.data.model.EventEntity
import kotlinx.coroutines.flow.Flow

/**
 * 事件数据访问对象
 */
@Dao
interface EventDao {

    @Insert
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Long): EventEntity?

    @Query("SELECT * FROM events WHERE startTime BETWEEN :start AND :end ORDER BY startTime ASC")
    suspend fun getEventsBetween(start: Long, end: Long): List<EventEntity>

    @Query("SELECT * FROM events WHERE startTime BETWEEN :start AND :end ORDER BY startTime ASC")
    fun getEventsBetweenFlow(start: Long, end: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY startTime DESC")
    suspend fun getAllEvents(): List<EventEntity>

    @Query("SELECT * FROM events ORDER BY startTime DESC")
    fun getAllEventsFlow(): Flow<List<EventEntity>>

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
}
