package com.calendar.ddcalendar.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.calendar.ddcalendar.data.model.Event
import com.calendar.ddcalendar.data.model.ReminderRule
import com.calendar.ddcalendar.data.model.ReminderType
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * 提醒调度器
 * 负责安排和取消事件提醒
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val workManager = WorkManager.getInstance(context)

    /**
     * 为事件安排提醒
     * 如果当前时间在事件开始和结束之间，立即发送通知
     */
    fun scheduleReminder(event: Event) {
        val currentTime = LocalDateTime.now()
        val startTimeMillis = event.startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTimeMillis = event.endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val currentTimeMillis = System.currentTimeMillis()

        // 检查当前时间是否在事件时间段内
        if (currentTime.isAfter(event.startTime) && currentTime.isBefore(event.endTime)) {
            // 立即发送通知
            NotificationHelper.showReminderNotification(
                context,
                event.id,
                event.title,
                event.description ?: "日程进行中"
            )
            return
        }

        // 如果事件已结束，不发送通知
        if (currentTimeMillis >= endTimeMillis) {
            return
        }

        // 计算提醒时间
        val reminderTime = if (event.reminderRule.minutesBefore < 0) {
            // 无提醒规则，在开始时间提醒
            event.startTime
        } else {
            // 按提醒规则提前提醒
            event.startTime.minusMinutes(event.reminderRule.minutesBefore.toLong())
        }
        
        val reminderTimeMillis = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // 如果提醒时间已过但事件未开始，在开始时间提醒
        if (reminderTimeMillis <= currentTimeMillis && currentTimeMillis < startTimeMillis) {
            val delay = startTimeMillis - currentTimeMillis
            scheduleNotification(event, delay)
            return
        }

        // 如果提醒时间在未来，按计划提醒
        if (reminderTimeMillis > currentTimeMillis) {
            val delay = reminderTimeMillis - currentTimeMillis
            scheduleNotification(event, delay)
        }
    }

    /**
     * 安排通知
     */
    private fun scheduleNotification(event: Event, delayMillis: Long) {
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "event_id" to event.id,
                    "event_title" to event.title,
                    "event_description" to (event.description ?: "")
                )
            )
            .addTag("reminder_${event.id}")
            .build()

        workManager.enqueue(workRequest)
    }

    /**
     * 取消事件提醒
     */
    fun cancelReminder(eventId: Long) {
        workManager.cancelAllWorkByTag("reminder_$eventId")
    }

    /**
     * 更新事件提醒
     */
    fun updateReminder(event: Event) {
        cancelReminder(event.id)
        scheduleReminder(event)
    }
}

/**
 * 提醒 Worker
 * 在指定时间触发通知
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val eventId = inputData.getLong("event_id", -1)
        val eventTitle = inputData.getString("event_title") ?: ""
        val eventDescription = inputData.getString("event_description") ?: ""

        if (eventId == -1L) {
            return Result.failure()
        }

        // 显示通知
        NotificationHelper.showReminderNotification(
            applicationContext,
            eventId,
            eventTitle,
            eventDescription
        )

        return Result.success()
    }
}
