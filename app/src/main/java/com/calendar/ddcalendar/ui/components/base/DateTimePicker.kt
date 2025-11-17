package com.calendar.ddcalendar.ui.components.base

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 日期时间选择器对话框（合并版）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    initialDateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDateTime.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(initialDateTime.toLocalTime()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期和时间") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 日期选择
                Text(
                    text = "日期",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                DatePickerContent(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )
                
                HorizontalDivider()
                
                // 时间选择
                Text(
                    text = "时间",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                TimePickerContent(
                    selectedTime = selectedTime,
                    onTimeSelected = { selectedTime = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDateTimeSelected(LocalDateTime.of(selectedDate, selectedTime))
                    onDismiss()
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 日期选择器内容
 */
@Composable
private fun DatePickerContent(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var year by remember { mutableStateOf(selectedDate.year) }
    var month by remember { mutableStateOf(selectedDate.monthValue) }
    var day by remember { mutableStateOf(selectedDate.dayOfMonth) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 年份选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("年份", style = MaterialTheme.typography.bodyLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { year-- }) { Text("-") }
                Text("$year", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { year++ }) { Text("+") }
            }
        }

        // 月份选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("月份", style = MaterialTheme.typography.bodyLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { if (month > 1) month-- }) { Text("-") }
                Text("$month", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { if (month < 12) month++ }) { Text("+") }
            }
        }

        // 日期选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("日期", style = MaterialTheme.typography.bodyLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val maxDay = LocalDate.of(year, month, 1).lengthOfMonth()
                TextButton(onClick = { if (day > 1) day-- }) { Text("-") }
                Text("$day", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { if (day < maxDay) day++ }) { Text("+") }
            }
        }

        // 更新选中日期
        LaunchedEffect(year, month, day) {
            try {
                val maxDay = LocalDate.of(year, month, 1).lengthOfMonth()
                val validDay = day.coerceIn(1, maxDay)
                onDateSelected(LocalDate.of(year, month, validDay))
            } catch (e: Exception) {
                // 忽略无效日期
            }
        }
    }
}

/**
 * 时间选择器内容
 */
@Composable
private fun TimePickerContent(
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    var hour by remember { mutableStateOf(selectedTime.hour) }
    var minute by remember { mutableStateOf(selectedTime.minute) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 小时选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("小时", style = MaterialTheme.typography.bodyLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { hour = (hour - 1 + 24) % 24 }) { Text("-") }
                Text(String.format("%02d", hour), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { hour = (hour + 1) % 24 }) { Text("+") }
            }
        }

        // 分钟选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("分钟", style = MaterialTheme.typography.bodyLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { minute = (minute - 5 + 60) % 60 }) { Text("-5") }
                Text(String.format("%02d", minute), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { minute = (minute + 5) % 60 }) { Text("+5") }
            }
        }

        // 更新选中时间
        LaunchedEffect(hour, minute) {
            onTimeSelected(LocalTime.of(hour, minute))
        }
    }
}
