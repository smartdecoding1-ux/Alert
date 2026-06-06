package com.alert.app

import android.app.Application
import com.alert.app.notifications.NotificationHelper
import com.alert.app.worker.MonitoringWorker

class AlertApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        MonitoringWorker.schedulePeriodicWork(this)
    }
}
