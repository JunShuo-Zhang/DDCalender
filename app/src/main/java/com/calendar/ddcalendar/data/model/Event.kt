package com.calendar.ddcalendar.data.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 日程事件业务模型
 * 用于 ViewModel 和 UI 层
 */
data class Event(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val allDay: Boolean = false,
    val color: Int? = null,
    val repeatRule: RepeatRule? = null,
    val repeatEnd: LocalDateTime? = null,
    val reminderRule: ReminderRule = ReminderRule.NONE,
    val reminderType: ReminderType = ReminderType.NOTIFICATION
) {
    /**
     * 转换为 EventEntity（数据库实体）
     */
    fun toEntity(): EventEntity {
        return EventEntity(
            id = id,
            title = title,
            description = description,
            startTime = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endTime = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            allDay = allDay,
            color = color,
            repeatRule = repeatRule?.serialize(),
            repeatEnd = repeatEnd?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            reminderMinutes = if (reminderRule == ReminderRule.NONE) null else reminderRule.minutesBefore,
            reminderType = reminderType.value
        )
    }

    companion object {
        /**
         * 从 EventEntity 创建 Event
         */
        fun fromEntity(entity: EventEntity): Event {
            val zoneId = ZoneId.systemDefault()
            return Event(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                startTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(entity.startTime), 
                    zoneId
                ),
                endTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(entity.endTime), 
                    zoneId
                ),
                allDay = entity.allDay,
                color = entity.color,
                repeatRule = RepeatRule.deserialize(entity.repeatRule),
                repeatEnd = entity.repeatEnd?.let { 
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(it), zoneId) 
                },
                reminderRule = ReminderRule.fromMinutes(entity.reminderMinutes),
                reminderType = ReminderType.fromValue(entity.reminderType)
            )
        }
    }
}
