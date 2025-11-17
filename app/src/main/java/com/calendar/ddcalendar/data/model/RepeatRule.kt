package com.calendar.ddcalendar.data.model

import java.time.DayOfWeek

/**
 * 重复规则（基于 RFC5545 简化版）
 */
data class RepeatRule(
    val freq: Frequency,
    val interval: Int = 1,
    val byWeekDays: List<DayOfWeek>? = null
) {
    /**
     * 序列化为字符串（存储到数据库）
     */
    fun serialize(): String {
        val parts = mutableListOf<String>()
        parts.add("FREQ=${freq.name}")
        
        if (interval > 1) {
            parts.add("INTERVAL=$interval")
        }
        
        byWeekDays?.let { days ->
            if (days.isNotEmpty()) {
                val dayStr = days.joinToString(",") { 
                    when(it) {
                        DayOfWeek.MONDAY -> "MO"
                        DayOfWeek.TUESDAY -> "TU"
                        DayOfWeek.WEDNESDAY -> "WE"
                        DayOfWeek.THURSDAY -> "TH"
                        DayOfWeek.FRIDAY -> "FR"
                        DayOfWeek.SATURDAY -> "SA"
                        DayOfWeek.SUNDAY -> "SU"
                    }
                }
                parts.add("BYDAY=$dayStr")
            }
        }
        
        return parts.joinToString(";")
    }

    /**
     * 获取显示文本
     */
    fun getDisplayText(): String {
        return when (freq) {
            Frequency.DAILY -> if (interval == 1) "每天" else "每${interval}天"
            Frequency.WEEKLY -> {
                if (byWeekDays.isNullOrEmpty()) {
                    if (interval == 1) "每周" else "每${interval}周"
                } else {
                    val dayNames = byWeekDays.joinToString("、") { getDayName(it) }
                    "每周$dayNames"
                }
            }
            Frequency.MONTHLY -> if (interval == 1) "每月" else "每${interval}月"
            Frequency.YEARLY -> if (interval == 1) "每年" else "每${interval}年"
        }
    }

    private fun getDayName(day: DayOfWeek): String {
        return when (day) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
        }
    }

    companion object {
        /**
         * 从字符串反序列化
         */
        fun deserialize(ruleStr: String?): RepeatRule? {
            if (ruleStr.isNullOrBlank()) return null
            
            try {
                val parts = ruleStr.split(";").associate { part ->
                    val (key, value) = part.split("=")
                    key to value
                }
                
                val freq = Frequency.valueOf(parts["FREQ"] ?: return null)
                val interval = parts["INTERVAL"]?.toIntOrNull() ?: 1
                
                val byWeekDays = parts["BYDAY"]?.split(",")?.mapNotNull { day ->
                    when (day) {
                        "MO" -> DayOfWeek.MONDAY
                        "TU" -> DayOfWeek.TUESDAY
                        "WE" -> DayOfWeek.WEDNESDAY
                        "TH" -> DayOfWeek.THURSDAY
                        "FR" -> DayOfWeek.FRIDAY
                        "SA" -> DayOfWeek.SATURDAY
                        "SU" -> DayOfWeek.SUNDAY
                        else -> null
                    }
                }
                
                return RepeatRule(freq, interval, byWeekDays)
            } catch (e: Exception) {
                return null
            }
        }
    }
}

/**
 * 重复频率枚举
 */
enum class Frequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
