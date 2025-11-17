package com.calendar.ddcalendar.ui.calendar.week

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.calendar.ddcalendar.data.model.CalendarDate
import com.calendar.ddcalendar.ui.components.event.EventCard
import com.calendar.ddcalendar.utils.DateUtils
import com.calendar.ddcalendar.viewmodel.CalendarViewModel
import com.calendar.ddcalendar.viewmodel.ViewMode
import kotlin.math.abs

/**
 * 周视图界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onEventClick: (Long) -> Unit = {},
    onAddEventClick: (CalendarDate) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    // 确保周视图数据已加载
    LaunchedEffect(Unit) {
        viewModel.switchViewMode(ViewMode.WEEK)
    }

    var dragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = {
                            val prevWeekDate = selectedDate.toLocalDate().minusWeeks(1)
                            viewModel.onDateSelected(CalendarDate.from(prevWeekDate))
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "上一周",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = DateUtils.formatYearMonth(selectedDate.year, selectedDate.month),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = {
                            val nextWeekDate = selectedDate.toLocalDate().plusWeeks(1)
                            viewModel.onDateSelected(CalendarDate.from(nextWeekDate))
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "下一周",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { viewModel.goToToday() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "今",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddEventClick(selectedDate) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加事件")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val weekDates = uiState.weekDates.ifEmpty { 
                DateUtils.getWeekDates(selectedDate) 
            }
            
            val animatedOffset by animateFloatAsState(
                targetValue = if (isDragging) dragOffset.coerceIn(-100f, 100f) else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "dragOffset"
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .offset(x = animatedOffset.dp)
                    .alpha(1f - abs(animatedOffset) / 300f)
                    .pointerInput(selectedDate) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                isDragging = true
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                isDragging = false
                                if (abs(dragOffset) > 200) {
                                    if (dragOffset > 0) {
                                        // 向右滑动 - 上一周
                                        val prevWeekDate = selectedDate.toLocalDate().minusWeeks(1)
                                        viewModel.onDateSelected(CalendarDate.from(prevWeekDate))
                                    } else {
                                        // 向左滑动 - 下一周
                                        val nextWeekDate = selectedDate.toLocalDate().plusWeeks(1)
                                        viewModel.onDateSelected(CalendarDate.from(nextWeekDate))
                                    }
                                }
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                isDragging = false
                                dragOffset = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                dragOffset += dragAmount
                            }
                        )
                    }
            ) {
                // 周日期头部
                WeekDayHeader(
                    weekDates = weekDates,
                    selectedDate = selectedDate,
                    lunarDates = uiState.lunarDates,
                    eventsByDate = uiState.eventsByDate,
                    onDateClick = { date ->
                        if (!isDragging) {
                            viewModel.onDateSelected(date)
                        }
                    }
                )

                HorizontalDivider()

                // 时间轴和日程显示
                WeekTimeAxis(
                    weekDates = weekDates,
                    eventsByDate = uiState.eventsByDate,
                    selectedDate = selectedDate,
                    onEventClick = onEventClick,
                    onDateClick = { date ->
                        if (!isDragging) {
                            viewModel.onDateSelected(date)
                        }
                    }
                )
            }
        }
    }
}

/**
 * 周日期头部
 */
@Composable
private fun WeekDayHeader(
    weekDates: List<CalendarDate>,
    selectedDate: CalendarDate,
    lunarDates: Map<CalendarDate, com.calendar.ddcalendar.data.model.LunarDate>,
    eventsByDate: Map<CalendarDate, List<com.calendar.ddcalendar.data.model.Event>>,
    onDateClick: (CalendarDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDates.forEach { date ->
            val isSelected = DateUtils.isSameDay(date, selectedDate)
            val isToday = DateUtils.isToday(date)
            val hasEvents = eventsByDate[date]?.isNotEmpty() == true
            val lunarDate = lunarDates[date]
            val lunarText = lunarDate?.getDisplayText() ?: ""
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onDateClick(date) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 星期
                Text(
                    text = DateUtils.getDayOfWeekName(date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 日期
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surface
                            },
                            shape = MaterialTheme.shapes.medium
                        )
                        .then(
                            if (hasEvents) {
                                Modifier.border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.medium
                                )
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = date.day.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            textAlign = TextAlign.Center
                        )
                        // 农历日期
                        if (lunarText.isNotEmpty()) {
                            Text(
                                text = lunarText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
                                ),
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 周视图时间轴和日程显示
 */
@Composable
private fun WeekTimeAxis(
    weekDates: List<CalendarDate>,
    eventsByDate: Map<CalendarDate, List<com.calendar.ddcalendar.data.model.Event>>,
    selectedDate: CalendarDate,
    onEventClick: (Long) -> Unit,
    onDateClick: (CalendarDate) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // 当选中日期改变时，自动滚动到对应的日期
    LaunchedEffect(selectedDate) {
        val selectedIndex = weekDates.indexOfFirst { DateUtils.isSameDay(it, selectedDate) }
        if (selectedIndex >= 0) {
            coroutineScope.launch {
                listState.animateScrollToItem(selectedIndex)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 为每一天显示时间轴和日程
        itemsIndexed(weekDates) { index, date ->
            val dateEvents = eventsByDate[date] ?: emptyList()
            val isSelected = DateUtils.isSameDay(date, selectedDate)
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDateClick(date) }
                    .padding(vertical = 8.dp)
            ) {
                // 日期标题
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${date.month}月${date.day}日",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = DateUtils.getDayOfWeekName(date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (DateUtils.isToday(date)) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "今天",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${dateEvents.size}个日程",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 显示该日期的所有日程
                if (dateEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "无日程",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        dateEvents.forEach { event ->
                            EventCard(
                                event = event,
                                onClick = { onEventClick(event.id) }
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
fun PreviewWeekScreen() {
    MaterialTheme {
        Surface {
            Text("周视图预览")
        }
    }
}
