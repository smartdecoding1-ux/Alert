package com.alert.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.alert.app.notifications.NotificationHelper

class AlertActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_DISMISS = "com.alert.app.ACTION_DISMISS_ALERT"
        const val ACTION_SNOOZE = "com.alert.app.ACTION_SNOOZE_ALERT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_DISMISS -> {
                NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIF_ID_DANGER)
                NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIF_ID_WARNING)
            }
            ACTION_SNOOZE -> {
                NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIF_ID_DANGER)
                NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIF_ID_WARNING)
            }
        }
    }
}
