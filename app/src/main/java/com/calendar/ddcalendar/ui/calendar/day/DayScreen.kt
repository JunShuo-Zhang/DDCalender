package com.calendar.ddcalendar.ui.calendar.day

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
import com.calendar.ddcalendar.data.model.CalendarDate
import com.calendar.ddcalendar.ui.components.event.EventCard
import com.calendar.ddcalendar.utils.DateUtils
import com.calendar.ddcalendar.viewmodel.CalendarViewModel
import com.calendar.ddcalendar.viewmodel.ViewMode
import kotlin.math.abs

/**
 * 日视图界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onEventClick: (Long) -> Unit = {},
    onAddEventClick: (CalendarDate) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    // 确保日视图数据已加载
    LaunchedEffect(Unit) {
        viewModel.switchViewMode(ViewMode.DAY)
    }

    var dragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = DateUtils.formatDate(selectedDate),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = DateUtils.getDayOfWeekName(selectedDate),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        uiState.selectedDateLunar?.let { lunar ->
                            Text(
                                text = lunar.getDisplayText(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val prevDate = selectedDate.toLocalDate().minusDays(1)
                            viewModel.onDateSelected(CalendarDate.from(prevDate))
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "前一天",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
                    IconButton(
                        onClick = {
                            val nextDate = selectedDate.toLocalDate().plusDays(1)
                            viewModel.onDateSelected(CalendarDate.from(nextDate))
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "后一天",
                            tint = MaterialTheme.colorScheme.primary
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
                                        // 向右滑动 - 前一天
                                        val prevDate = selectedDate.toLocalDate().minusDays(1)
                                        viewModel.onDateSelected(CalendarDate.from(prevDate))
                                    } else {
                                        // 向左滑动 - 后一天
                                        val nextDate = selectedDate.toLocalDate().plusDays(1)
                                        viewModel.onDateSelected(CalendarDate.from(nextDate))
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
                val events = uiState.dayEvents

                if (events.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
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
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(events) { event ->
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
fun PreviewDayScreen() {
    MaterialTheme {
        Surface {
            Text("日视图预览")
        }
    }
}
