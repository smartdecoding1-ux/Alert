package com.alert.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatEpochMillis(millis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun timeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            else -> "${diff / 86_400_000}d ago"
        }
    }
}
