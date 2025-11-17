package com.calendar.ddcalendar.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 日程事件实体（Room 数据库表）
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    
    val title: String,
    val description: String? = null,
    val startTime: Long,  // epoch millis
    val endTime: Long,    // epoch millis
    
    val allDay: Boolean = false,
    val color: Int? = null,   // 标签色（可选）
    
    // 重复相关
    val repeatRule: String? = null, // 序列化的 RFC5545 子集
    val repeatEnd: Long? = null,    // 重复事件截止日期
    
    // 提醒相关
    val reminderMinutes: Int? = null,  // 提前多少分钟提醒
    val reminderType: Int = 0  // 提醒类型：0=仅通知, 1=震动, 2=响铃, 3=震动+响铃
)
