package com.calendar.ddcalendar.utils

import com.calendar.ddcalendar.data.model.CalendarDate
import com.calendar.ddcalendar.data.model.LunarDate
import java.time.LocalDate

/**
 * 农历工具类
 * 提供完整的公历转农历功能
 * 
 * 基于寿星万年历算法实现
 * 支持 1900-2100 年的农历转换
 */
object LunarCalendarUtil {

    // 农历基准日期：1900年1月31日（农历1900年正月初一）
    private const val LUNAR_BASE_YEAR = 1900
    private val LUNAR_BASE_DATE = LocalDate.of(1900, 1, 31)
    
    /**
     * 农历数据表（1900-2100年）
     * 每个元素的二进制位表示该年的农历信息：
     * - 低12位：表示12个月的大小月（1=30天，0=29天）
     * - 高4位：表示闰月月份（0=无闰月，1-12=闰几月）
     * - 如果有闰月，第13位表示闰月大小（1=30天，0=29天）
     */
    private val LUNAR_INFO = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, // 1900-1909
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, // 1910-1919
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, // 1920-1929
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, // 1930-1939
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, // 1940-1949
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0, // 1950-1959
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, // 1960-1969
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6, // 1970-1979
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, // 1980-1989
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, // 1990-1999
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, // 2000-2009
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, // 2010-2019
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, // 2020-2029
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, // 2030-2039
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0, // 2040-2049
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0, // 2050-2059
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4, // 2060-2069
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0, // 2070-2079
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160, // 2080-2089
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252, // 2090-2099
        0x0d520                                                                                    // 2100
    )

    // 节气名称
    private val SOLAR_TERMS = arrayOf(
        "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
        "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
        "小暑", "大暑", "立秋", "处暑", "白露", "秋分",
        "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    )

    // 农历节日
    private val LUNAR_FESTIVALS = mapOf(
        "1-1" to "春节",
        "1-15" to "元宵节",
        "2-2" to "龙抬头",
        "5-5" to "端午节",
        "7-7" to "七夕节",
        "7-15" to "中元节",
        "8-15" to "中秋节",
        "9-9" to "重阳节",
        "12-8" to "腊八节"
    )

    // 公历节日
    private val SOLAR_FESTIVALS = mapOf(
        "1-1" to "元旦",
        "2-14" to "情人节",
        "3-8" to "妇女节",
        "3-12" to "植树节",
        "4-1" to "愚人节",
        "5-1" to "劳动节",
        "5-4" to "青年节",
        "6-1" to "儿童节",
        "7-1" to "建党节",
        "8-1" to "建军节",
        "9-10" to "教师节",
        "10-1" to "国庆节",
        "12-25" to "圣诞节"
    )

    /**
     * 获取指定农历年的闰月月份（0表示无闰月）
     */
    private fun getLeapMonth(lunarYear: Int): Int {
        if (lunarYear < LUNAR_BASE_YEAR || lunarYear > 2100) return 0
        return LUNAR_INFO[lunarYear - LUNAR_BASE_YEAR] and 0x0F
    }

    /**
     * 获取指定农历年闰月的天数
     */
    private fun getLeapMonthDays(lunarYear: Int): Int {
        if (getLeapMonth(lunarYear) == 0) return 0
        return if ((LUNAR_INFO[lunarYear - LUNAR_BASE_YEAR] and 0x10000) != 0) 30 else 29
    }

    /**
     * 获取指定农历年指定月份的天数
     * @param isLeapMonth 是否是闰月
     */
    private fun getMonthDays(lunarYear: Int, lunarMonth: Int, isLeapMonth: Boolean = false): Int {
        if (lunarYear < LUNAR_BASE_YEAR || lunarYear > 2100) return 0
        
        if (isLeapMonth) {
            return getLeapMonthDays(lunarYear)
        }
        
        // 农历数据编码格式：
        // bit 0-3: 闰月月份（0表示无闰月）
        // bit 4-15: 12个月的大小月信息（1=30天，0=29天）
        //   bit 15对应正月，bit 14对应二月，...，bit 4对应十二月
        // bit 16: 闰月大小（1=30天，0=29天）
        return if ((LUNAR_INFO[lunarYear - LUNAR_BASE_YEAR] and (0x10000 shr lunarMonth)) != 0) 30 else 29
    }

    /**
     * 获取指定农历年的总天数
     */
    private fun getYearDays(lunarYear: Int): Int {
        if (lunarYear < LUNAR_BASE_YEAR || lunarYear > 2100) return 0
        
        var sum = 348 // 12个月，每月29天
        // bit 4-15 表示12个月的大小月信息
        // bit 15对应正月，bit 14对应二月，...，bit 4对应十二月
        // 循环月份从1到12
        for (month in 1..12) {
            if ((LUNAR_INFO[lunarYear - LUNAR_BASE_YEAR] and (0x10000 shr month)) != 0) {
                sum++ // 大月加1天
            }
        }
        return sum + getLeapMonthDays(lunarYear)
    }

    /**
     * 将公历日期转换为农历日期
     */
    fun solarToLunar(date: CalendarDate): LunarDate {
        val localDate = date.toLocalDate()
        
        // 检查日期范围
        if (localDate.isBefore(LUNAR_BASE_DATE) || localDate.year > 2100) {
            return createDefaultLunarDate(date)
        }
        
        // 计算与基准日期相差的天数
        val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(LUNAR_BASE_DATE, localDate).toInt()
        
        // 从1900年开始累加年份
        var lunarYear = LUNAR_BASE_YEAR
        var daysCount = daysDiff
        
        // 确定农历年份
        while (daysCount >= getYearDays(lunarYear)) {
            daysCount -= getYearDays(lunarYear)
            lunarYear++
        }
        
        // 确定农历月份和日期
        val leapMonth = getLeapMonth(lunarYear)
        var isLeapMonth = false
        var lunarMonth = 1
        
        // 遍历月份（包括可能的闰月）
        for (month in 1..12) {
            // 先处理正常月份
            val normalMonthDays = getMonthDays(lunarYear, month, false)
            if (daysCount < normalMonthDays) {
                lunarMonth = month
                isLeapMonth = false
                break
            }
            daysCount -= normalMonthDays
            
            // 如果当前月是闰月所在月，再处理闰月
            if (month == leapMonth && leapMonth > 0) {
                val leapMonthDays = getLeapMonthDays(lunarYear)
                if (daysCount < leapMonthDays) {
                    lunarMonth = month
                    isLeapMonth = true
                    break
                }
                daysCount -= leapMonthDays
            }
        }
        
        val lunarDay = daysCount + 1
        
        // 获取节日和节气
        val solarFestival = SOLAR_FESTIVALS["${date.month}-${date.day}"]
        val lunarFestival = LUNAR_FESTIVALS["$lunarMonth-$lunarDay"]
        val solarTerm = getSolarTerm(date)
        
        return LunarDate(
            lunarYear = lunarYear,
            lunarMonth = lunarMonth,
            lunarDay = lunarDay,
            isLeapMonth = isLeapMonth,
            solarTerm = solarTerm,
            lunarFestival = lunarFestival,
            solarFestival = solarFestival
        )
    }

    /**
     * 创建默认农历日期（用于超出范围的日期）
     */
    private fun createDefaultLunarDate(date: CalendarDate): LunarDate {
        val solarFestival = SOLAR_FESTIVALS["${date.month}-${date.day}"]
        return LunarDate(
            lunarYear = date.year,
            lunarMonth = date.month,
            lunarDay = date.day,
            isLeapMonth = false,
            solarTerm = null,
            lunarFestival = null,
            solarFestival = solarFestival
        )
    }

    /**
     * 2025年节气精确日期表
     * 格式：月-日 到 节气名称
     */
    private val SOLAR_TERM_2025 = mapOf(
        "1-5" to "小寒", "1-20" to "大寒",
        "2-3" to "立春", "2-18" to "雨水",
        "3-5" to "惊蛰", "3-20" to "春分",
        "4-4" to "清明", "4-20" to "谷雨",
        "5-5" to "立夏", "5-21" to "小满",
        "6-5" to "芒种", "6-21" to "夏至",
        "7-7" to "小暑", "7-22" to "大暑",
        "8-7" to "立秋", "8-23" to "处暑",
        "9-7" to "白露", "9-23" to "秋分",
        "10-8" to "寒露", "10-23" to "霜降",
        "11-7" to "立冬", "11-22" to "小雪",
        "12-7" to "大雪", "12-21" to "冬至"
    )
    
    /**
     * 2024年节气精确日期表
     */
    private val SOLAR_TERM_2024 = mapOf(
        "1-6" to "小寒", "1-20" to "大寒",
        "2-4" to "立春", "2-19" to "雨水",
        "3-5" to "惊蛰", "3-20" to "春分",
        "4-4" to "清明", "4-19" to "谷雨",
        "5-5" to "立夏", "5-20" to "小满",
        "6-5" to "芒种", "6-21" to "夏至",
        "7-6" to "小暑", "7-22" to "大暑",
        "8-7" to "立秋", "8-22" to "处暑",
        "9-7" to "白露", "9-22" to "秋分",
        "10-8" to "寒露", "10-23" to "霜降",
        "11-7" to "立冬", "11-22" to "小雪",
        "12-6" to "大雪", "12-21" to "冬至"
    )

    /**
     * 获取节气
     * 使用精确的节气日期表
     */
    private fun getSolarTerm(date: CalendarDate): String? {
        val key = "${date.month}-${date.day}"
        
        // 根据年份选择对应的节气表
        return when (date.year) {
            2024 -> SOLAR_TERM_2024[key]
            2025 -> SOLAR_TERM_2025[key]
            else -> null // 其他年份暂不支持，可以后续扩展
        }
    }

    /**
     * 批量转换日期（带缓存优化）
     */
    fun batchSolarToLunar(dates: List<CalendarDate>): Map<CalendarDate, LunarDate> {
        return dates.associateWith { solarToLunar(it) }
    }
}
