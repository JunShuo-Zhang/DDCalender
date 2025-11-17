package com.calendar.ddcalendar.data.model

/**
 * 提醒类型枚举
 */
enum class ReminderType(val displayText: String, val value: Int) {
    NOTIFICATION("仅通知", 0),
    VIBRATE("震动", 1),
    SOUND("响铃", 2),
    VIBRATE_AND_SOUND("震动+响铃", 3);

    companion object {
        fun fromValue(value: Int): ReminderType {
            return entries.find { it.value == value } ?: NOTIFICATION
        }
    }
}
