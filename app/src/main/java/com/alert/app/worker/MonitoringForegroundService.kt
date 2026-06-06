package com.alert.app.worker

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.alert.app.notifications.NotificationHelper

class MonitoringForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        val notification = NotificationHelper.buildMonitoringNotification(this)
        startForeground(NotificationHelper.NOTIF_ID_MONITORING, notification)
        MonitoringWorker.schedulePeriodicWork(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        MonitoringWorker.schedulePeriodicWork(this)
    }
}
