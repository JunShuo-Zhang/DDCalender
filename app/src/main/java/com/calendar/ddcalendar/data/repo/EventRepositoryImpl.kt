package com.calendar.ddcalendar.data.repo

import com.calendar.ddcalendar.data.local.EventDao
import com.calendar.ddcalendar.data.model.CalendarDate
import com.calendar.ddcalendar.data.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 事件仓库实现
 */
@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : EventRepository {

    override suspend fun addEvent(event: Event): Long {
        return eventDao.insertEvent(event.toEntity())
    }

    override suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event.toEntity())
    }

    override suspend fun deleteEvent(id: Long) {
        eventDao.deleteEventById(id)
    }

    override suspend fun getEventById(id: Long): Event? {
        return eventDao.getEventById(id)?.let { Event.fromEntity(it) }
    }

    override suspend fun getEventsInRange(
        startDate: CalendarDate,
        endDate: CalendarDate
    ): List<Event> {
        val startMillis = startDate.toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        val endMillis = endDate.toLocalDate()
            .atTime(LocalTime.MAX)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        return eventDao.getEventsBetween(startMillis, endMillis)
            .map { Event.fromEntity(it) }
    }

    override fun getEventsInRangeFlow(
        startDate: CalendarDate,
        endDate: CalendarDate
    ): Flow<List<Event>> {
        val startMillis = startDate.toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        val endMillis = endDate.toLocalDate()
            .atTime(LocalTime.MAX)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        return eventDao.getEventsBetweenFlow(startMillis, endMillis)
            .map { entities -> entities.map { Event.fromEntity(it) } }
    }

    override suspend fun getEventsForDate(date: CalendarDate): List<Event> {
        return getEventsInRange(date, date)
    }

    override fun getEventsForDateFlow(date: CalendarDate): Flow<List<Event>> {
        return getEventsInRangeFlow(date, date)
    }

    override suspend fun getAllEvents(): List<Event> {
        return eventDao.getAllEvents().map { Event.fromEntity(it) }
    }

    override fun getAllEventsFlow(): Flow<List<Event>> {
        return eventDao.getAllEventsFlow()
            .map { entities -> entities.map { Event.fromEntity(it) } }
    }
}
