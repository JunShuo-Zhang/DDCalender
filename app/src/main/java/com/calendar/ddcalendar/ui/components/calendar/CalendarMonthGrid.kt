package com.calendar.ddcalendar.ui.components.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.calendar.ddcalendar.data.model.CalendarDate
import com.calendar.ddcalendar.data.model.LunarDate
import com.calendar.ddcalendar.utils.DateUtils

/**
 * 月视图网格组件
 * 显示完整的月历网格（7x6）
 */
@Composable
fun CalendarMonthGrid(
    dates: List<CalendarDate>,
    selectedDate: CalendarDate,
    currentYear: Int,
    currentMonth: Int,
    lunarDates: Map<CalendarDate, LunarDate>,
    eventsByDate: Map<CalendarDate, List<Any>>,
    modifier: Modifier = Modifier,
    onDateClick: (CalendarDate) -> Unit = {},
    onDateLongPress: (CalendarDate) -> Unit = {}
) {
    Column(modifier = modifier) {
        // 星期标题行
        WeekDayHeader()

        Spacer(modifier = Modifier.height(8.dp))

        // 日期网格（6行7列）- 优化间距
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            dates.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { date ->
                        val isToday = DateUtils.isToday(date)
                        val isSelected = DateUtils.isSameDay(date, selectedDate)
                        val isInCurrentMonth = DateUtils.isInMonth(date, currentYear, currentMonth)
                        val hasEvents = eventsByDate[date]?.isNotEmpty() == true
                        val lunarText = lunarDates[date]?.getDisplayText() ?: ""

                        CalendarDayItem(
                            date = date,
                            isToday = isToday,
                            isSelected = isSelected,
                            isInCurrentMonth = isInCurrentMonth,
                            hasEvents = hasEvents,
                            lunarText = lunarText,
                            modifier = Modifier.weight(1f),
                            onClick = { onDateClick(date) },
                            onLongPress = { onDateLongPress(date) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 星期标题行 - 优化样式
 */
@Composable
private fun WeekDayHeader() {
    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        weekDays.forEachIndexed { index, day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                ),
                color = if (index == 0 || index == 6) {
                    // 周末使用主题色
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCalendarMonthGrid() {
    MaterialTheme {
        val dates = DateUtils.getMonthDates(2025, 3)
        CalendarMonthGrid(
            dates = dates,
            selectedDate = CalendarDate.today(),
            currentYear = 2025,
            currentMonth = 3,
            lunarDates = emptyMap(),
            eventsByDate = emptyMap(),
            modifier = Modifier.padding(16.dp)
        )
    }
}
