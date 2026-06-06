package com.alert.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alert.app.notifications.NotificationHelper
import com.alert.app.worker.MonitoringForegroundService
import com.alert.app.worker.MonitoringWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                NotificationHelper.createNotificationChannels(context)
                MonitoringWorker.schedulePeriodicWork(context)

                val serviceIntent = Intent(context, MonitoringForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
