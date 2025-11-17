package com.calendar.ddcalendar

import android.app.Application
import com.calendar.ddcalendar.utils.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用程序入口
 */
@HiltAndroidApp
class CalendarApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // 创建通知渠道
        NotificationHelper.createNotificationChannel(this)
    }
}
