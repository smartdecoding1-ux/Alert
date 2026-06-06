package com.alert.app.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alert.app.data.models.*
import com.alert.app.data.repository.AlertRepository
import com.alert.app.data.repository.PreferencesRepository
import com.alert.app.data.repository.Result
import com.alert.app.data.repository.UserPreferences
import com.alert.app.notifications.NotificationHelper
import com.alert.app.utils.AlertSoundManager
import com.alert.app.worker.MonitoringForegroundService
import com.alert.app.worker.MonitoringWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlertRepository()
    private val prefsRepository = PreferencesRepository(application)

    private val _monitoringState = MutableStateFlow(MonitoringState())
    val monitoringState: StateFlow<MonitoringState> = _monitoringState.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = prefsRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private val _isEmergencyActive = MutableStateFlow(false)
    val isEmergencyActive: StateFlow<Boolean> = _isEmergencyActive.asStateFlow()

    init {
        NotificationHelper.createNotificationChannels(application)
        startBackgroundMonitoring()
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _monitoringState.update { it.copy(isLoading = true, error = null) }

            val prefs = userPreferences.value
            val city = prefs.city.ifEmpty { "Kushinagar" }

            var weatherData: WeatherData? = null
            var aqiData: AqiData2? = null
            var earthquakeData: EarthquakeData? = null
            var errorMsg: String? = null

            // Fetch weather
            when (val result = repository.fetchWeather(city)) {
                is Result.Success -> weatherData = result.data
                is Result.Error -> errorMsg = result.message
                else -> {}
            }

            // Fetch AQI
            when (val result = repository.fetchAqi(city)) {
                is Result.Success -> aqiData = result.data
                is Result.Error -> if (errorMsg == null) errorMsg = result.message
                else -> {}
            }

            // Fetch earthquakes
            when (val result = repository.fetchEarthquakes()) {
                is Result.Success -> earthquakeData = result.data
                is Result.Error -> if (errorMsg == null) errorMsg = result.message
                else -> {}
            }

            val alertState = repository.evaluateAlertLevel(
                weather = weatherData,
                aqi = aqiData,
                earthquake = earthquakeData,
                aqiThreshold = prefs.aqiThreshold,
                tempThreshold = prefs.temperatureThreshold
            )

            _monitoringState.update {
                it.copy(
                    weather = weatherData,
                    aqi = aqiData,
                    earthquake = earthquakeData,
                    alertState = alertState,
                    isLoading = false,
                    lastRefresh = System.currentTimeMillis(),
                    error = errorMsg
                )
            }

            handleAlertTriggered(alertState, prefs)
        }
    }

    private fun handleAlertTriggered(alertState: AlertState, prefs: UserPreferences) {
        when (alertState.level) {
            AlertLevel.DANGER -> {
                _isEmergencyActive.value = true
                if (prefs.notificationsEnabled) {
                    NotificationHelper.sendAlertNotification(getApplication(), alertState, true)
                }
                if (prefs.alarmSoundEnabled) {
                    AlertSoundManager.playAlarmSound(getApplication())
                }
                if (prefs.vibrationEnabled) {
                    AlertSoundManager.startVibration(getApplication())
                }
            }
            AlertLevel.WARNING -> {
                _isEmergencyActive.value = false
                AlertSoundManager.stopAll()
                if (prefs.notificationsEnabled) {
                    NotificationHelper.sendAlertNotification(getApplication(), alertState, true)
                }
            }
            AlertLevel.SAFE -> {
                _isEmergencyActive.value = false
                AlertSoundManager.stopAll()
            }
        }
    }

    fun dismissEmergency() {
        _isEmergencyActive.value = false
        AlertSoundManager.stopAll()
        NotificationHelper.sendAlertNotification(
            getApplication(),
            AlertState(level = AlertLevel.SAFE),
            true
        )
    }

    fun startBackgroundMonitoring() {
        MonitoringWorker.schedulePeriodicWork(getApplication())
        val serviceIntent = Intent(getApplication(), MonitoringForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(serviceIntent)
        } else {
            getApplication<Application>().startService(serviceIntent)
        }
    }

    fun stopBackgroundMonitoring() {
        MonitoringWorker.cancelWork(getApplication())
    }

    // Preference updates
    fun updateCity(city: String) {
        viewModelScope.launch {
            prefsRepository.updateCity(city)
            refreshAll()
        }
    }

    fun updateAqiThreshold(threshold: Int) {
        viewModelScope.launch { prefsRepository.updateAqiThreshold(threshold) }
    }

    fun updateTemperatureThreshold(threshold: Double) {
        viewModelScope.launch { prefsRepository.updateTemperatureThreshold(threshold) }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.updateNotificationsEnabled(enabled) }
    }

    fun updateAlarmSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.updateAlarmSoundEnabled(enabled) }
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.updateVibrationEnabled(enabled) }
    }

    fun updateBackgroundMonitoringEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.updateBackgroundMonitoringEnabled(enabled)
            if (enabled) startBackgroundMonitoring() else stopBackgroundMonitoring()
        }
    }
}
