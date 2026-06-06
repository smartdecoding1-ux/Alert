package com.alert.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "alert_preferences")

data class UserPreferences(
    val city: String = "Kushinagar",
    val aqiThreshold: Int = 100,
    val temperatureThreshold: Double = 40.0,
    val notificationsEnabled: Boolean = true,
    val alarmSoundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val backgroundMonitoringEnabled: Boolean = true
)

class PreferencesRepository(private val context: Context) {

    private object PreferenceKeys {
        val CITY = stringPreferencesKey("city")
        val AQI_THRESHOLD = intPreferencesKey("aqi_threshold")
        val TEMP_THRESHOLD = floatPreferencesKey("temp_threshold")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ALARM_SOUND_ENABLED = booleanPreferencesKey("alarm_sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val BG_MONITORING_ENABLED = booleanPreferencesKey("bg_monitoring_enabled")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                city = preferences[PreferenceKeys.CITY] ?: "Kushinagar",
                aqiThreshold = preferences[PreferenceKeys.AQI_THRESHOLD] ?: 100,
                temperatureThreshold = (preferences[PreferenceKeys.TEMP_THRESHOLD] ?: 40.0f).toDouble(),
                notificationsEnabled = preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true,
                alarmSoundEnabled = preferences[PreferenceKeys.ALARM_SOUND_ENABLED] ?: true,
                vibrationEnabled = preferences[PreferenceKeys.VIBRATION_ENABLED] ?: true,
                backgroundMonitoringEnabled = preferences[PreferenceKeys.BG_MONITORING_ENABLED] ?: true
            )
        }

    suspend fun updateCity(city: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.CITY] = city
        }
    }

    suspend fun updateAqiThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.AQI_THRESHOLD] = threshold
        }
    }

    suspend fun updateTemperatureThreshold(threshold: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.TEMP_THRESHOLD] = threshold.toFloat()
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateAlarmSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.ALARM_SOUND_ENABLED] = enabled
        }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun updateBackgroundMonitoringEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.BG_MONITORING_ENABLED] = enabled
        }
    }
}
