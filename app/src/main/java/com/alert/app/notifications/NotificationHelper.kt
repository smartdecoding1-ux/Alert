package com.alert.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alert.app.MainActivity
import com.alert.app.R
import com.alert.app.data.models.AlertLevel
import com.alert.app.data.models.AlertState

object NotificationHelper {

    const val CHANNEL_ID_GENERAL = "alert_general"
    const val CHANNEL_ID_WARNING = "alert_warning"
    const val CHANNEL_ID_DANGER = "alert_danger"
    const val CHANNEL_ID_MONITORING = "alert_monitoring"

    const val NOTIF_ID_MONITORING = 1001
    const val NOTIF_ID_WARNING = 2001
    const val NOTIF_ID_DANGER = 3001
    const val NOTIF_ID_WEATHER = 4001
    const val NOTIF_ID_AQI = 4002
    const val NOTIF_ID_EARTHQUAKE = 4003

    fun createNotificationChannels(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // General channel
        NotificationChannel(
            CHANNEL_ID_GENERAL,
            "General Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "General environmental alerts"
            notificationManager.createNotificationChannel(this)
        }

        // Warning channel
        NotificationChannel(
            CHANNEL_ID_WARNING,
            "Warning Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Warning level alerts"
            enableVibration(true)
            notificationManager.createNotificationChannel(this)
        }

        // Danger channel
        NotificationChannel(
            CHANNEL_ID_DANGER,
            "DANGER Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Critical danger alerts"
            enableVibration(true)
            setShowBadge(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(this)
        }

        // Background monitoring channel
        NotificationChannel(
            CHANNEL_ID_MONITORING,
            "Background Monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent background monitoring status"
            setShowBadge(false)
            notificationManager.createNotificationChannel(this)
        }
    }

    fun buildMonitoringNotification(context: Context): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID_MONITORING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Alert - Active Monitoring")
            .setContentText("Monitoring weather, AQI, and earthquakes")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    fun sendAlertNotification(
        context: Context,
        alertState: AlertState,
        notificationsEnabled: Boolean = true
    ) {
        if (!notificationsEnabled) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_EMERGENCY", alertState.level == AlertLevel.DANGER)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        when (alertState.level) {
            AlertLevel.DANGER -> {
                val messages = listOfNotNull(
                    alertState.weatherAlert,
                    alertState.aqiAlert,
                    alertState.earthquakeAlert
                )
                val body = if (messages.isNotEmpty()) messages.joinToString("\n") 
                           else "EMERGENCY ALERT: Dangerous environmental conditions detected."

                val notification = NotificationCompat.Builder(context, CHANNEL_ID_DANGER)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("🚨 EMERGENCY ALERT")
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setFullScreenIntent(pendingIntent, true)
                    .build()

                try {
                    NotificationManagerCompat.from(context).notify(NOTIF_ID_DANGER, notification)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }

            AlertLevel.WARNING -> {
                val messages = listOfNotNull(
                    alertState.weatherAlert,
                    alertState.aqiAlert,
                    alertState.earthquakeAlert
                )
                val body = if (messages.isNotEmpty()) messages.joinToString("\n")
                           else "Warning: Environmental conditions require attention."

                val notification = NotificationCompat.Builder(context, CHANNEL_ID_WARNING)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("⚠️ Environmental Warning")
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .build()

                try {
                    NotificationManagerCompat.from(context).notify(NOTIF_ID_WARNING, notification)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }

            AlertLevel.SAFE -> {
                NotificationManagerCompat.from(context).cancel(NOTIF_ID_WARNING)
                NotificationManagerCompat.from(context).cancel(NOTIF_ID_DANGER)
            }
        }
    }

    fun sendSpecificNotification(context: Context, title: String, body: String, notifId: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WARNING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
