package com.alert.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alert.app.data.repository.UserPreferences
import com.alert.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onNavigateBack: () -> Unit,
    onCityChange: (String) -> Unit,
    onAqiThresholdChange: (Int) -> Unit,
    onTempThresholdChange: (Double) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onAlarmSoundChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onBackgroundMonitoringChange: (Boolean) -> Unit
) {
    var cityInput by remember(preferences.city) { mutableStateOf(preferences.city) }
    var aqiInput by remember(preferences.aqiThreshold) { mutableStateOf(preferences.aqiThreshold.toString()) }
    var tempInput by remember(preferences.temperatureThreshold) { mutableStateOf(preferences.temperatureThreshold.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = {
                Text("Settings", fontWeight = FontWeight.Bold, color = Color.White)
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location Settings
            SettingsSection(title = "Location", icon = Icons.Default.LocationOn, iconColor = PrimaryBlue) {
                OutlinedTextField(
                    value = cityInput,
                    onValueChange = { cityInput = it },
                    label = { Text("City Name") },
                    placeholder = { Text("e.g. Kushinagar") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = OutlineDark,
                        focusedLabelColor = PrimaryBlue,
                        cursorColor = PrimaryBlue,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedLabelColor = OnSurfaceDark
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (cityInput.isNotBlank()) onCityChange(cityInput.trim())
                        }) {
                            Icon(Icons.Default.Check, "Save city", tint = PrimaryBlue)
                        }
                    },
                    singleLine = true
                )
                Text(
                    "Default: Kushinagar, Uttar Pradesh, India",
                    fontSize = 12.sp,
                    color = OnSurfaceDark.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // Threshold Settings
            SettingsSection(title = "Alert Thresholds", icon = Icons.Default.Tune, iconColor = WarningOrange) {
                Text(
                    "AQI Warning Threshold",
                    fontSize = 13.sp,
                    color = OnSurfaceDark,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = aqiInput,
                    onValueChange = { aqiInput = it },
                    label = { Text("AQI Threshold (default: 100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = settingsTextFieldColors(),
                    trailingIcon = {
                        IconButton(onClick = {
                            aqiInput.toIntOrNull()?.let { onAqiThresholdChange(it) }
                        }) {
                            Icon(Icons.Default.Check, "Save", tint = WarningOrange)
                        }
                    },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Temperature Warning Threshold (°C)",
                    fontSize = 13.sp,
                    color = OnSurfaceDark,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = tempInput,
                    onValueChange = { tempInput = it },
                    label = { Text("Temperature °C (default: 40)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = settingsTextFieldColors(),
                    trailingIcon = {
                        IconButton(onClick = {
                            tempInput.toDoubleOrNull()?.let { onTempThresholdChange(it) }
                        }) {
                            Icon(Icons.Default.Check, "Save", tint = WarningOrange)
                        }
                    },
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))
                ThresholdInfoRow("SAFE", "AQI < ${preferences.aqiThreshold} | Temp < ${preferences.temperatureThreshold}°C | No EQ ≥ 5.0", SafeGreen)
                ThresholdInfoRow("WARNING", "AQI ${preferences.aqiThreshold}-200 | Temp ${preferences.temperatureThreshold}-45°C | EQ 5.0-6.0", WarningOrange)
                ThresholdInfoRow("DANGER", "AQI > 200 | Temp > 45°C | EQ > 6.0", DangerRed)
            }

            // Notifications
            SettingsSection(title = "Notifications", icon = Icons.Default.Notifications, iconColor = PrimaryBlue) {
                SettingsToggle(
                    label = "Push Notifications",
                    description = "Receive alerts on device",
                    checked = preferences.notificationsEnabled,
                    onCheckedChange = onNotificationsChange
                )
                HorizontalDivider(color = OutlineDark, modifier = Modifier.padding(vertical = 8.dp))
                SettingsToggle(
                    label = "Alarm Sound",
                    description = "Play alarm sound on DANGER",
                    checked = preferences.alarmSoundEnabled,
                    onCheckedChange = onAlarmSoundChange
                )
                HorizontalDivider(color = OutlineDark, modifier = Modifier.padding(vertical = 8.dp))
                SettingsToggle(
                    label = "Vibration",
                    description = "Vibrate on WARNING and DANGER",
                    checked = preferences.vibrationEnabled,
                    onCheckedChange = onVibrationChange
                )
            }

            // Background Service
            SettingsSection(title = "Background Monitoring", icon = Icons.Default.CloudSync, iconColor = SafeGreen) {
                SettingsToggle(
                    label = "Background Monitoring",
                    description = "Check APIs every 15 minutes",
                    checked = preferences.backgroundMonitoringEnabled,
                    onCheckedChange = onBackgroundMonitoringChange
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Background Features:", fontSize = 12.sp, color = OnSurfaceDark, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        BulletPoint("Monitors weather, AQI, and earthquakes every 15 min")
                        BulletPoint("Continues after app is closed")
                        BulletPoint("Auto-restarts after device reboot")
                        BulletPoint("Battery-optimized via WorkManager")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ThresholdInfoRow(label: String, info: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.width(72.dp)
        ) {
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(info, fontSize = 11.sp, color = OnSurfaceDark.copy(alpha = 0.7f))
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("• ", fontSize = 12.sp, color = SafeGreen)
        Text(text, fontSize = 12.sp, color = OnSurfaceDark.copy(alpha = 0.8f))
    }
}

@Composable
fun SettingsToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 12.sp, color = OnSurfaceDark.copy(alpha = 0.6f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = OutlineDark
            )
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            content()
        }
    }
}

@Composable
fun settingsTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = WarningOrange,
    unfocusedBorderColor = OutlineDark,
    focusedLabelColor = WarningOrange,
    cursorColor = WarningOrange,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    unfocusedLabelColor = OnSurfaceDark
)
