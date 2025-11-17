package com.calendar.ddcalendar.data.repo

import com.calendar.ddcalendar.data.model.CalendarDate
import com.calendar.ddcalendar.data.model.Event
import kotlinx.coroutines.flow.Flow

/**
 * 事件仓库接口
 */
interface EventRepository {

    /**
     * 添加事件
     */
    suspend fun addEvent(event: Event): Long

    /**
     * 更新事件
     */
    suspend fun updateEvent(event: Event)

    /**
     * 删除事件
     */
    suspend fun deleteEvent(id: Long)

    /**
     * 根据 ID 获取事件
     */
    suspend fun getEventById(id: Long): Event?

    /**
     * 获取指定日期范围内的事件
     */
    suspend fun getEventsInRange(
        startDate: CalendarDate,
        endDate: CalendarDate
    ): List<Event>

    /**
     * 获取指定日期范围内的事件（Flow）
     */
    fun getEventsInRangeFlow(
        startDate: CalendarDate,
        endDate: CalendarDate
    ): Flow<List<Event>>

    /**
     * 获取指定日期的事件
     */
    suspend fun getEventsForDate(date: CalendarDate): List<Event>

    /**
     * 获取指定日期的事件（Flow）
     */
    fun getEventsForDateFlow(date: CalendarDate): Flow<List<Event>>

    /**
     * 获取所有事件
     */
    suspend fun getAllEvents(): List<Event>

    /**
     * 获取所有事件（Flow）
     */
    fun getAllEventsFlow(): Flow<List<Event>>
}
