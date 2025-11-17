package com.calendar.ddcalendar.ui.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.calendar.ddcalendar.data.model.Event
import com.calendar.ddcalendar.data.model.ReminderRule
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 事件卡片组件
 * 用于显示单个事件的信息
 */
@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val eventColor = event.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 颜色标签条 - 更粗更明显
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(eventColor)
            )

            // 事件信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 标题 - 更粗的字体
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 时间 - 使用更精致的样式
                val timeFormatter = if (event.allDay) {
                    DateTimeFormatter.ofPattern("全天")
                } else {
                    DateTimeFormatter.ofPattern("HH:mm")
                }
                
                val timeText = if (event.allDay) {
                    "全天"
                } else {
                    "${event.startTime.format(timeFormatter)} - ${event.endTime.format(timeFormatter)}"
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = eventColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            ),
                            color = eventColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // 描述（如果有）
                event.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.2f
                        )
                    }
                }

                // 图标行（提醒、重复）- 更精致的样式
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (event.reminderRule != ReminderRule.NONE) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "有提醒",
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(4.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (event.repeatRule != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "重复事件",
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(4.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEventCard() {
    MaterialTheme {
        EventCard(
            event = Event(
                id = 1,
                title = "团队会议",
                description = "讨论项目进度和下一步计划",
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusHours(1),
                allDay = false,
                color = 0xFF3F51B5.toInt(),
                reminderRule = ReminderRule.MIN_10
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
