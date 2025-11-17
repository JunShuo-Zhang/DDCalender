package com.calendar.ddcalendar.ui.components.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.calendar.ddcalendar.data.model.CalendarDate

/**
 * 单个日期格子组件
 * 用于月视图显示
 *
 * @param date 日期对象
 * @param isToday 是否为今日
 * @param isSelected 是否被选中
 * @param isInCurrentMonth 是否在当前月份
 * @param hasEvents 是否有日程
 * @param lunarText 农历文本
 * @param onClick 点击回调
 * @param onLongPress 长按回调
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarDayItem(
    date: CalendarDate,
    isToday: Boolean,
    isSelected: Boolean,
    isInCurrentMonth: Boolean,
    hasEvents: Boolean,
    lunarText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .then(
                // 有日程时显示边框，选中时背景与边框叠加
                if (hasEvents) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 3.dp, vertical = 5.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // 公历日期 - 加粗显示，当天用圆形背景
            Box(
                modifier = Modifier
                    .then(
                        if (isToday) {
                            Modifier
                                .size(28.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.day.toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    color = when {
                        isToday -> MaterialTheme.colorScheme.onPrimary
                        !isInCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center
                )
            }

            // 农历文本 - 紧贴公历下方，颜色较浅
            if (lunarText.isNotEmpty()) {
                Text(
                    text = lunarText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f
                    ),
                    color = when {
                        isToday -> MaterialTheme.colorScheme.primary
                        !isInCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 0.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun PreviewCalendarDayItem_Today() {
    MaterialTheme {
        CalendarDayItem(
            date = CalendarDate(2025, 3, 15),
            isToday = true,
            isSelected = false,
            isInCurrentMonth = true,
            hasEvents = true,
            lunarText = "初五"
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun PreviewCalendarDayItem_Selected() {
    MaterialTheme {
        CalendarDayItem(
            date = CalendarDate(2025, 3, 20),
            isToday = false,
            isSelected = true,
            isInCurrentMonth = true,
            hasEvents = false,
            lunarText = "初十"
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun PreviewCalendarDayItem_OtherMonth() {
    MaterialTheme {
        CalendarDayItem(
            date = CalendarDate(2025, 2, 28),
            isToday = false,
            isSelected = false,
            isInCurrentMonth = false,
            hasEvents = false,
            lunarText = "廿九"
        )
    }
}
