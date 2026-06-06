package com.alert.app.worker

import android.content.Context
import androidx.work.*
import com.alert.app.data.models.AlertLevel
import com.alert.app.data.repository.AlertRepository
import com.alert.app.data.repository.PreferencesRepository
import com.alert.app.notifications.NotificationHelper
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class MonitoringWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository = AlertRepository()
    private val prefsRepository = PreferencesRepository(context)

    override suspend fun doWork(): Result {
        return try {
            val prefs = prefsRepository.userPreferencesFlow.first()

            if (!prefs.backgroundMonitoringEnabled) return Result.success()

            val city = prefs.city.ifEmpty { "Kushinagar" }

            val weatherResult = repository.fetchWeather(city)
            val aqiResult = repository.fetchAqi(city)
            val earthquakeResult = repository.fetchEarthquakes()

            val weather = (weatherResult as? com.alert.app.data.repository.Result.Success)?.data
            val aqi = (aqiResult as? com.alert.app.data.repository.Result.Success)?.data
            val earthquake = (earthquakeResult as? com.alert.app.data.repository.Result.Success)?.data

            val alertState = repository.evaluateAlertLevel(
                weather = weather,
                aqi = aqi,
                earthquake = earthquake,
                aqiThreshold = prefs.aqiThreshold,
                tempThreshold = prefs.temperatureThreshold
            )

            if (alertState.level != AlertLevel.SAFE && prefs.notificationsEnabled) {
                NotificationHelper.sendAlertNotification(
                    context = context,
                    alertState = alertState,
                    notificationsEnabled = prefs.notificationsEnabled
                )
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_TAG = "alert_monitoring_work"
        const val UNIQUE_WORK_NAME = "AlertMonitoringPeriodicWork"

        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<MonitoringWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(WORK_TAG)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )
        }

        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }

        fun scheduleImmediateWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeWork = OneTimeWorkRequestBuilder<MonitoringWorker>()
                .setConstraints(constraints)
                .addTag("alert_immediate_check")
                .build()

            WorkManager.getInstance(context).enqueue(oneTimeWork)
        }
    }
}
