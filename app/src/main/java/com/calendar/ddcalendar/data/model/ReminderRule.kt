package com.calendar.ddcalendar.data.model

/**
 * 提醒规则枚举
 * 定义提前多少分钟提醒
 */
enum class ReminderRule(val minutesBefore: Int, val displayText: String) {
    NONE(-1, "无提醒"),
    AT_TIME(0, "准时"),
    MIN_5(5, "提前5分钟"),
    MIN_10(10, "提前10分钟"),
    MIN_30(30, "提前30分钟"),
    HOUR_1(60, "提前1小时"),
    DAY_1(1440, "提前1天");

    companion object {
        /**
         * 根据分钟数获取提醒规则
         */
        fun fromMinutes(minutes: Int?): ReminderRule {
            if (minutes == null) return NONE
            return entries.find { it.minutesBefore == minutes } ?: NONE
        }
    }
}
