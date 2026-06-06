package com.alert.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alert.app.data.models.AlertLevel
import com.alert.app.ui.dashboard.DashboardScreen
import com.alert.app.ui.emergency.EmergencyScreen
import com.alert.app.ui.settings.SettingsScreen
import com.alert.app.ui.theme.AlertTheme
import com.alert.app.ui.theme.BackgroundDark
import com.alert.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        val openEmergency = intent.getBooleanExtra("OPEN_EMERGENCY", false)

        setContent {
            AlertTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    AlertApp(
                        viewModel = viewModel,
                        startAtEmergency = openEmergency
                    )
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun AlertApp(
    viewModel: MainViewModel,
    startAtEmergency: Boolean = false
) {
    val navController = rememberNavController()
    val monitoringState by viewModel.monitoringState.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val isEmergencyActive by viewModel.isEmergencyActive.collectAsState()

    // Auto-navigate to emergency when danger detected
    LaunchedEffect(isEmergencyActive) {
        if (isEmergencyActive) {
            navController.navigate("emergency") {
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (startAtEmergency) "emergency" else "dashboard"
    ) {
        composable(
            "dashboard",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            DashboardScreen(
                monitoringState = monitoringState,
                onRefresh = viewModel::refreshAll,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable(
            "settings",
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { it } }
        ) {
            SettingsScreen(
                preferences = userPreferences,
                onNavigateBack = navController::popBackStack,
                onCityChange = viewModel::updateCity,
                onAqiThresholdChange = viewModel::updateAqiThreshold,
                onTempThresholdChange = viewModel::updateTemperatureThreshold,
                onNotificationsChange = viewModel::updateNotificationsEnabled,
                onAlarmSoundChange = viewModel::updateAlarmSoundEnabled,
                onVibrationChange = viewModel::updateVibrationEnabled,
                onBackgroundMonitoringChange = viewModel::updateBackgroundMonitoringEnabled
            )
        }

        composable(
            "emergency",
            enterTransition = { fadeIn() + slideInVertically { it } },
            exitTransition = { fadeOut() + slideOutVertically { it } }
        ) {
            EmergencyScreen(
                alertState = monitoringState.alertState,
                weather = monitoringState.weather,
                aqi = monitoringState.aqi,
                earthquake = monitoringState.earthquake,
                onDismiss = {
                    viewModel.dismissEmergency()
                    navController.navigate("dashboard") {
                        popUpTo("emergency") { inclusive = true }
                    }
                }
            )
        }
    }
}
