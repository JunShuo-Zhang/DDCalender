package com.calendar.ddcalendar.ui.event

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calendar.ddcalendar.data.model.CalendarDate
import com.calendar.ddcalendar.data.model.Event
import com.calendar.ddcalendar.data.model.ReminderRule
import com.calendar.ddcalendar.ui.components.base.DateTimePickerDialog
import com.calendar.ddcalendar.viewmodel.CalendarViewModel
import com.calendar.ddcalendar.viewmodel.EventViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 事件编辑界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorScreen(
    eventId: Long? = null,
    initialDate: CalendarDate? = null,
    viewModel: EventViewModel = hiltViewModel(),
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    // 优先使用传入的日期，否则使用ViewModel中选中的日期
    val selectedDateFromViewModel by calendarViewModel.selectedDate.collectAsState()
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // 初始化时间，使用传入的日期或当前选中的日期
    // 注意：这里使用selectedDateFromViewModel作为初始值，后续通过LaunchedEffect更新
    val now = LocalDateTime.now()
    var startTime by remember { 
        mutableStateOf(
            LocalDateTime.of(
                selectedDateFromViewModel.year,
                selectedDateFromViewModel.month,
                selectedDateFromViewModel.day,
                now.hour,
                0
            )
        )
    }
    var endTime by remember { 
        mutableStateOf(
            LocalDateTime.of(
                selectedDateFromViewModel.year,
                selectedDateFromViewModel.month,
                selectedDateFromViewModel.day,
                (now.hour + 1).coerceAtMost(23),
                0
            )
        )
    }
    var allDay by remember { mutableStateOf(false) }
    var reminderRule by remember { mutableStateOf(ReminderRule.NONE) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showTimeErrorDialog by remember { mutableStateOf(false) }
    
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")

    val uiState by viewModel.uiState.collectAsState()

    // 如果是新建模式且传入了日期，更新默认时间
    LaunchedEffect(initialDate, eventId) {
        // 只有在新建模式（eventId == null）且传入了日期时才更新
        if (eventId == null && initialDate != null) {
            val now = LocalDateTime.now()
            startTime = LocalDateTime.of(
                initialDate.year,
                initialDate.month,
                initialDate.day,
                now.hour,
                0
            )
            endTime = LocalDateTime.of(
                initialDate.year,
                initialDate.month,
                initialDate.day,
                (now.hour + 1).coerceAtMost(23),
                0
            )
        }
    }

    // 如果是编辑模式，加载事件数据
    LaunchedEffect(eventId) {
        eventId?.let {
            viewModel.loadEvent(it)
        }
    }

    // 监听事件数据加载
    val currentEvent by viewModel.currentEvent.collectAsState()
    LaunchedEffect(currentEvent) {
        currentEvent?.let { event ->
            title = event.title
            description = event.description ?: ""
            startTime = event.startTime
            endTime = event.endTime
            allDay = event.allDay
            reminderRule = event.reminderRule
        }
    }

    // 保存成功后刷新并返回
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            calendarViewModel.refreshCurrentView()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eventId == null) "新建日程" else "编辑日程") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // 验证结束时间（仅针对非全天事件）
                            if (!allDay && !endTime.isAfter(startTime)) {
                                showTimeErrorDialog = true
                                return@IconButton
                            }
                            
                            val event = Event(
                                id = eventId ?: 0,
                                title = title,
                                description = description.ifBlank { null },
                                startTime = startTime,
                                endTime = endTime,
                                allDay = allDay,
                                reminderRule = reminderRule
                            )
                            if (eventId == null) {
                                viewModel.createEvent(event)
                            } else {
                                viewModel.updateEvent(event)
                            }
                        },
                        enabled = title.isNotBlank() && !uiState.isSaving
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题输入 - 优化样式
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            // 描述输入 - 优化样式
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("描述（可选）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            // 全天开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("全天", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = allDay,
                    onCheckedChange = { allDay = it }
                )
            }

            HorizontalDivider()

            // 时间选择区域（全天时隐藏）- 优化样式
            if (!allDay) {
                // 开始时间
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStartTimePicker = true },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "开始时间",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            startTime.format(dateTimeFormatter),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 结束时间
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEndTimePicker = true },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "结束时间",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            endTime.format(dateTimeFormatter),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                // 全天事件只显示日期
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStartTimePicker = true },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "日期",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            startTime.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            HorizontalDivider()

            // 提醒设置 - 优化样式
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showReminderDialog = true },
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "提醒时间",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        reminderRule.displayText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 提醒说明
            Text(
                text = "日程时间内将自动发送通知",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // 错误提示
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 加载指示器
            if (uiState.isSaving) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        // 开始时间选择器
        if (showStartTimePicker) {
            DateTimePickerDialog(
                initialDateTime = startTime,
                onDateTimeSelected = { newTime ->
                    startTime = newTime
                    // 如果结束时间早于开始时间，自动调整
                    if (endTime.isBefore(newTime)) {
                        endTime = newTime.plusHours(1)
                    }
                },
                onDismiss = { showStartTimePicker = false }
            )
        }

        // 结束时间选择器
        if (showEndTimePicker) {
            DateTimePickerDialog(
                initialDateTime = endTime,
                onDateTimeSelected = { newTime ->
                    endTime = newTime
                },
                onDismiss = { showEndTimePicker = false }
            )
        }

        // 提醒时间选择对话框
        if (showReminderDialog) {
            AlertDialog(
                onDismissRequest = { showReminderDialog = false },
                title = { Text("选择提醒时间") },
                text = {
                    Column {
                        ReminderRule.entries.forEach { rule ->
                            TextButton(
                                onClick = {
                                    reminderRule = rule
                                    showReminderDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(rule.displayText)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showReminderDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // 时间错误提示对话框
        if (showTimeErrorDialog) {
            AlertDialog(
                onDismissRequest = { showTimeErrorDialog = false },
                title = { Text("时间设置错误") },
                text = { Text("结束时间不合理，请重新设置") },
                confirmButton = {
                    TextButton(onClick = { showTimeErrorDialog = false }) {
                        Text("确定")
                    }
                }
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEventEditorScreen() {
    MaterialTheme {
        Surface {
            Text("事件编辑界面预览")
        }
    }
}
