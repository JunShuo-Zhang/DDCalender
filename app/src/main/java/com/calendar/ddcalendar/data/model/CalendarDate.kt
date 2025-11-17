package com.calendar.ddcalendar.data.model

import java.time.LocalDate

/**
 * 标准公历日期模型
 * 用于统一表示一个日期（年-月-日）
 */
data class CalendarDate(
    val year: Int,
    val month: Int, // 1~12
    val day: Int    // 1~31
) {
    /**
     * 转换为 LocalDate
     */
    fun toLocalDate(): LocalDate {
        return LocalDate.of(year, month, day)
    }

    companion object {
        /**
         * 从 LocalDate 创建 CalendarDate
         */
        fun from(localDate: LocalDate): CalendarDate {
            return CalendarDate(
                year = localDate.year,
                month = localDate.monthValue,
                day = localDate.dayOfMonth
            )
        }

        /**
         * 获取今天的日期
         */
        fun today(): CalendarDate {
            return from(LocalDate.now())
        }
    }
}
