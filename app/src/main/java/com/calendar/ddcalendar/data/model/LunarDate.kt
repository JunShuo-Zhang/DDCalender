package com.calendar.ddcalendar.data.model

/**
 * 农历日期模型
 * 用于支持农历显示、节气、节日
 */
data class LunarDate(
    val lunarYear: Int,
    val lunarMonth: Int,
    val lunarDay: Int,
    val isLeapMonth: Boolean = false,
    val solarTerm: String? = null,     // 节气（如"小寒"）
    val lunarFestival: String? = null, // 农历节日（如"除夕"）
    val solarFestival: String? = null  // 公历节日（如"元旦"）
) {
    /**
     * 获取农历日期的显示文本
     * 优先级：公历节日 > 农历节日 > 节气 > 农历日期
     */
    fun getDisplayText(): String {
        // 优先显示节日
        solarFestival?.let { return it }
        lunarFestival?.let { return it }
        solarTerm?.let { return it }
        
        // 显示农历日期
        return when (lunarDay) {
            1 -> getMonthText()
            else -> getDayText()
        }
    }

    /**
     * 获取完整的农历日期描述
     * 例如：甲辰年 八月十五 中秋节
     */
    fun getFullDescription(): String {
        val monthText = getMonthText()
        val dayText = getDayText()
        val festival = lunarFestival ?: solarFestival ?: ""
        return if (festival.isNotEmpty()) {
            "$monthText$dayText $festival"
        } else {
            "$monthText$dayText"
        }
    }

    private fun getMonthText(): String {
        val months = arrayOf("正月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "冬月", "腊月")
        return if (isLeapMonth) {
            "闰${months[lunarMonth - 1]}"
        } else {
            months[lunarMonth - 1]
        }
    }

    private fun getDayText(): String {
        val days = arrayOf(
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
        )
        return if (lunarDay in 1..30) days[lunarDay - 1] else ""
    }
}
