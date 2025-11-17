package com.calendar.ddcalendar

import com.calendar.ddcalendar.data.model.CalendarDate
import com.calendar.ddcalendar.utils.LunarCalendarUtil
import org.junit.Test
import org.junit.Assert.*

/**
 * 农历工具类测试
 * 验证公历转农历的准确性
 */
class LunarCalendarUtilTest {

    @Test
    fun testSpringFestival2024() {
        // 2024年春节：2024年2月10日 = 农历2024年正月初一
        val date = CalendarDate(2024, 2, 10)
        val lunar = LunarCalendarUtil.solarToLunar(date)
        
        assertEquals(2024, lunar.lunarYear)
        assertEquals(1, lunar.lunarMonth)
        assertEquals(1, lunar.lunarDay)
        assertEquals(false, lunar.isLeapMonth)
        assertEquals("春节", lunar.lunarFestival)
    }

    @Test
    fun testMidAutumnFestival2024() {
        // 2024年中秋节：2024年9月17日 = 农历2024年八月十五
        val date = CalendarDate(2024, 9, 17)
        val lunar = LunarCalendarUtil.solarToLunar(date)
        
        assertEquals(2024, lunar.lunarYear)
        assertEquals(8, lunar.lunarMonth)
        assertEquals(15, lunar.lunarDay)
        assertEquals("中秋节", lunar.lunarFestival)
    }

    @Test
    fun testSpringFestival2025() {
        // 2025年春节：2025年1月29日 = 农历2025年正月初一
        val date = CalendarDate(2025, 1, 29)
        val lunar = LunarCalendarUtil.solarToLunar(date)
        
        assertEquals(2025, lunar.lunarYear)
        assertEquals(1, lunar.lunarMonth)
        assertEquals(1, lunar.lunarDay)
        assertEquals("春节", lunar.lunarFestival)
    }

    @Test
    fun testNewYearDay() {
        // 2024年元旦：2024年1月1日 = 农历2023年十一月二十
        val date = CalendarDate(2024, 1, 1)
        val lunar = LunarCalendarUtil.solarToLunar(date)
        
        assertEquals(2023, lunar.lunarYear) // 农历年还是2023年
        assertEquals(11, lunar.lunarMonth)  // 十一月
        assertEquals(20, lunar.lunarDay)    // 二十
        assertEquals("元旦", lunar.solarFestival)
    }

    @Test
    fun testDragonBoatFestival2024() {
        // 2024年端午节：2024年6月10日 = 农历2024年五月初五
        val date = CalendarDate(2024, 6, 10)
        val lunar = LunarCalendarUtil.solarToLunar(date)
        
        assertEquals(2024, lunar.lunarYear)
        assertEquals(5, lunar.lunarMonth)
        assertEquals(5, lunar.lunarDay)
        assertEquals("端午节", lunar.lunarFestival)
    }

    @Test
    fun testSolarTerms() {
        // 测试节气
        // 2024年立春：2月4日
        val lichun = CalendarDate(2024, 2, 4)
        val lunarLichun = LunarCalendarUtil.solarToLunar(lichun)
        assertNotNull(lunarLichun.solarTerm)
        
        // 2024年冬至：12月21日左右
        val dongzhi = CalendarDate(2024, 12, 21)
        val lunarDongzhi = LunarCalendarUtil.solarToLunar(dongzhi)
        assertNotNull(lunarDongzhi.solarTerm)
    }

    @Test
    fun testBatchConversion() {
        // 测试批量转换
        val dates = listOf(
            CalendarDate(2024, 1, 1),
            CalendarDate(2024, 2, 10),
            CalendarDate(2024, 9, 17)
        )
        
        val lunarDates = LunarCalendarUtil.batchSolarToLunar(dates)
        
        assertEquals(3, lunarDates.size)
        assertTrue(lunarDates.containsKey(dates[0]))
        assertTrue(lunarDates.containsKey(dates[1]))
        assertTrue(lunarDates.containsKey(dates[2]))
    }

    @Test
    fun testDisplayText() {
        // 测试显示文本
        val springFestival = CalendarDate(2024, 2, 10)
        val lunar = LunarCalendarUtil.solarToLunar(springFestival)
        
        // 春节应该显示"春节"
        assertEquals("春节", lunar.getDisplayText())
        
        // 普通日期应该显示农历日期
        val normalDay = CalendarDate(2024, 3, 15)
        val lunarNormal = LunarCalendarUtil.solarToLunar(normalDay)
        assertTrue(lunarNormal.getDisplayText().isNotEmpty())
    }

    @Test
    fun testBoundaryDates() {
        // 测试边界日期
        // 1900年1月31日（农历基准日期）
        val baseDate = CalendarDate(1900, 1, 31)
        val lunar = LunarCalendarUtil.solarToLunar(baseDate)
        assertEquals(1900, lunar.lunarYear)
        assertEquals(1, lunar.lunarMonth)
        assertEquals(1, lunar.lunarDay)
        
        // 测试2100年的日期
        val futureDate = CalendarDate(2100, 12, 31)
        val lunarFuture = LunarCalendarUtil.solarToLunar(futureDate)
        assertNotNull(lunarFuture)
    }

    @Test
    fun testOutOfRangeDates() {
        // 测试超出范围的日期（应该返回默认值）
        val tooEarly = CalendarDate(1899, 12, 31)
        val lunar = LunarCalendarUtil.solarToLunar(tooEarly)
        assertNotNull(lunar)
        
        val tooLate = CalendarDate(2101, 1, 1)
        val lunarLate = LunarCalendarUtil.solarToLunar(tooLate)
        assertNotNull(lunarLate)
    }

    @Test
    fun testNovember16_2025() {
        // 测试2025年11月16日的农历日期（应该是农历九月廿七）
        val date = CalendarDate(2025, 11, 16)
        val lunar = LunarCalendarUtil.solarToLunar(date)
        
        // 打印实际结果用于调试
        println("2025-11-16 => 农历${lunar.lunarYear}年${lunar.lunarMonth}月${lunar.lunarDay}日")
        println("显示文本: ${lunar.getDisplayText()}")
        
        // 验证农历日期
        assertEquals(9, lunar.lunarMonth)   // 九月
        assertEquals(27, lunar.lunarDay)    // 廿七
        assertEquals(false, lunar.isLeapMonth)
        
        // 验证显示文本应该是"廿七"
        val displayText = lunar.getDisplayText()
        assertTrue(displayText.contains("廿七") || displayText == "廿七")
    }
}
