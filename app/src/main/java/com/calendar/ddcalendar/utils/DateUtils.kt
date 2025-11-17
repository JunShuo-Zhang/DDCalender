package com.calendar.ddcalendar.utils

import com.calendar.ddcalendar.data.model.CalendarDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * 日期工具类
 * 提供日期计算、格式化等功能
 */
object DateUtils {

    /**
     * 获取指定年月的所有日期（包含前后填充）
     * 用于月视图显示
     */
    fun getMonthDates(year: Int, month: Int): List<CalendarDate> {
        val yearMonth = YearMonth.of(year, month)
        val firstDayOfMonth = yearMonth.atDay(1)
        val lastDayOfMonth = yearMonth.atEndOfMonth()
        
        // 获取第一天是星期几（1=周一，7=周日）
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
        
        // 计算需要显示的第一天（从周日开始）
        val startDate = if (firstDayOfWeek == 7) {
            firstDayOfMonth
        } else {
            firstDayOfMonth.minusDays(firstDayOfWeek.toLong())
        }
        
        // 计算需要显示的最后一天（6行7列 = 42天）
        val dates = mutableListOf<CalendarDate>()
        var currentDate = startDate
        
        repeat(42) {
            dates.add(CalendarDate.from(currentDate))
            currentDate = currentDate.plusDays(1)
        }
        
        return dates
    }

    /**
     * 获取指定日期所在周的所有日期（周日到周六）
     */
    fun getWeekDates(date: CalendarDate): List<CalendarDate> {
        val localDate = date.toLocalDate()
        val sunday = localDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        
        return (0..6).map { offset ->
            CalendarDate.from(sunday.plusDays(offset.toLong()))
        }
    }

    /**
     * 判断两个日期是否是同一天
     */
    fun isSameDay(date1: CalendarDate, date2: CalendarDate): Boolean {
        return date1.year == date2.year && 
               date1.month == date2.month && 
               date1.day == date2.day
    }

    /**
     * 判断日期是否是今天
     */
    fun isToday(date: CalendarDate): Boolean {
        val today = LocalDate.now()
        return date.year == today.year && 
               date.month == today.monthValue && 
               date.day == today.dayOfMonth
    }

    /**
     * 判断日期是否在指定月份
     */
    fun isInMonth(date: CalendarDate, year: Int, month: Int): Boolean {
        return date.year == year && date.month == month
    }

    /**
     * 格式化日期为字符串
     */
    fun formatDate(date: CalendarDate, pattern: String = "yyyy年M月d日"): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return date.toLocalDate().format(formatter)
    }

    /**
     * 格式化年月
     */
    fun formatYearMonth(year: Int, month: Int): String {
        return "${year}年${month}月"
    }

    /**
     * 获取星期几的中文名称
     */
    fun getDayOfWeekName(date: CalendarDate): String {
        return when (date.toLocalDate().dayOfWeek) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
        }
    }

    /**
     * 获取下个月
     */
    fun getNextMonth(year: Int, month: Int): Pair<Int, Int> {
        return if (month == 12) {
            Pair(year + 1, 1)
        } else {
            Pair(year, month + 1)
        }
    }

    /**
     * 获取上个月
     */
    fun getPreviousMonth(year: Int, month: Int): Pair<Int, Int> {
        return if (month == 1) {
            Pair(year - 1, 12)
        } else {
            Pair(year, month - 1)
        }
    }
}
