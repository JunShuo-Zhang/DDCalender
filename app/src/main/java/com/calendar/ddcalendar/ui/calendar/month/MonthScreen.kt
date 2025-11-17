package com.calendar.ddcalendar.ui.calendar.month

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calendar.ddcalendar.data.model.CalendarDate as AppCalendarDate
import com.calendar.ddcalendar.ui.components.base.YearMonthPickerDialog
import com.calendar.ddcalendar.ui.components.calendar.CalendarMonthGrid
import com.calendar.ddcalendar.ui.components.event.EventCard
import com.calendar.ddcalendar.utils.DateUtils
import com.calendar.ddcalendar.viewmodel.CalendarViewModel
import kotlin.math.abs

/**
 * 月视图界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onEventClick: (Long) -> Unit = {},
    onAddEventClick: (AppCalendarDate) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val (currentYear, currentMonth) = viewModel.currentYearMonth.collectAsState().value
    
    var showYearMonthPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.goToPreviousMonth() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "上个月",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = DateUtils.formatYearMonth(currentYear, currentMonth),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable { showYearMonthPicker = true }
                        )

                        IconButton(
                            onClick = { viewModel.goToNextMonth() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "下个月",
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
                containerColor = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加事件",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
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
            var dragOffset by remember { mutableStateOf(0f) }
            var isDragging by remember { mutableStateOf(false) }
            val animatedOffset by animateFloatAsState(
                targetValue = if (isDragging) dragOffset.coerceIn(-100f, 100f) else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "dragOffset"
            )
            
            // 监听uiState变化，确保事件列表自动更新
            // 当eventsByDate更新时，selectedDateEvents会自动重新计算
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .offset(x = animatedOffset.dp)
                    .alpha(1f - abs(animatedOffset) / 300f)
                    .pointerInput(currentYear, currentMonth) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                isDragging = true
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                isDragging = false
                                if (abs(dragOffset) > 200) {
                                    if (dragOffset > 0) {
                                        // 向右滑动 - 上个月
                                        viewModel.goToPreviousMonth()
                                    } else {
                                        // 向左滑动 - 下个月
                                        viewModel.goToNextMonth()
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
                // 月历网格
                CalendarMonthGrid(
                    dates = uiState.monthDates,
                    selectedDate = selectedDate,
                    currentYear = currentYear,
                    currentMonth = currentMonth,
                    lunarDates = uiState.lunarDates,
                    eventsByDate = uiState.eventsByDate,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    onDateClick = { date ->
                        if (!isDragging) {
                            viewModel.onDateSelected(date)
                        }
                    },
                    onDateLongPress = { date ->
                        if (!isDragging) {
                            viewModel.onDateSelected(date)
                            onAddEventClick(date)
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // 选中日期的事件列表
                // 直接使用uiState.eventsByDate确保数据更新时自动刷新
                val selectedDateEvents = uiState.eventsByDate[selectedDate] ?: emptyList()

                if (selectedDateEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "当天无日程",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "${DateUtils.formatDate(selectedDate)}的日程",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(
                            items = selectedDateEvents,
                            key = { event -> event.id } // 为每个item添加key，确保列表更新时正确重组
                        ) { event ->
                            EventCard(
                                event = event,
                                onClick = { onEventClick(event.id) }
                            )
                        }
                    }
                }
            }
        }
        
        // 年份月份选择对话框
        if (showYearMonthPicker) {
            YearMonthPickerDialog(
                initialYear = currentYear,
                initialMonth = currentMonth,
                onYearMonthSelected = { year, month ->
                    viewModel.goToYearMonth(year, month)
                },
                onDismiss = { showYearMonthPicker = false }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMonthScreen() {
    MaterialTheme {
        // Preview 需要提供 ViewModel，这里仅作为布局预览
        Surface {
            Text("月视图预览")
        }
    }
}
