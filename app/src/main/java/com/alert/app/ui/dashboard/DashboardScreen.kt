package com.alert.app.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alert.app.data.models.*
import com.alert.app.ui.theme.*
import com.alert.app.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    monitoringState: MonitoringState,
    onRefresh: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val alertColor = when (monitoringState.alertState.level) {
        AlertLevel.SAFE -> SafeGreen
        AlertLevel.WARNING -> WarningOrange
        AlertLevel.DANGER -> DangerRed
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = alertColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "ALERT",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.White,
                        letterSpacing = 3.sp
                    )
                }
            },
            actions = {
                IconButton(onClick = onRefresh) {
                    if (monitoringState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SurfaceDark
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Banner
            AlertStatusBanner(
                alertState = monitoringState.alertState,
                alertColor = alertColor
            )

            // Error message
            monitoringState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3A1A1A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = DangerRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(error, color = Color(0xFFFFAAAD), fontSize = 12.sp)
                    }
                }
            }

            // Weather Card
            WeatherCard(weather = monitoringState.weather)

            // AQI Card
            AqiCard(aqi = monitoringState.aqi)

            // Earthquake Card
            EarthquakeCard(earthquake = monitoringState.earthquake)

            // System Status Card
            SystemStatusCard(
                lastRefresh = monitoringState.lastRefresh,
                isLoading = monitoringState.isLoading,
                alertState = monitoringState.alertState
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun AlertStatusBanner(alertState: AlertState, alertColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner")
    val alpha by infiniteTransition.animateFloat(
        initialValue = if (alertState.level == AlertLevel.DANGER) 0.6f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = alertColor.copy(alpha = 0.15f)
        ),
        border = BorderStroke(
            width = if (alertState.level == AlertLevel.DANGER) 2.dp else 1.dp,
            color = alertColor.copy(alpha = if (alertState.level == AlertLevel.DANGER) alpha else 1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(alertColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (alertState.level) {
                        AlertLevel.SAFE -> "✅"
                        AlertLevel.WARNING -> "⚠️"
                        AlertLevel.DANGER -> "🚨"
                    },
                    fontSize = 22.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (alertState.level) {
                        AlertLevel.SAFE -> "ALL SYSTEMS SAFE"
                        AlertLevel.WARNING -> "WARNING ACTIVE"
                        AlertLevel.DANGER -> "DANGER — EMERGENCY"
                    },
                    color = alertColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
                val messages = listOfNotNull(
                    alertState.weatherAlert,
                    alertState.aqiAlert,
                    alertState.earthquakeAlert
                )
                if (messages.isEmpty()) {
                    Text(
                        "Environmental conditions within safe parameters",
                        color = alertColor.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                } else {
                    messages.forEach { msg ->
                        Text(msg, color = alertColor.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherCard(weather: WeatherData?) {
    MonitoringCard(
        title = "Weather",
        icon = Icons.Default.Cloud,
        iconColor = PrimaryBlue,
        isLoading = weather == null
    ) {
        if (weather != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${String.format("%.1f", weather.temperature)}°C",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = weather.condition,
                        fontSize = 14.sp,
                        color = OnSurfaceDark
                    )
                    Text(
                        text = weather.description.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        color = OnSurfaceDark.copy(alpha = 0.7f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = weather.city,
                        fontSize = 14.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Uttar Pradesh, India", fontSize = 11.sp, color = OnSurfaceDark.copy(alpha = 0.6f))
                }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = OutlineDark)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherStat("Feels Like", "${String.format("%.1f", weather.feelsLike)}°C", Icons.Default.Thermostat)
                WeatherStat("Humidity", "${weather.humidity}%", Icons.Default.WaterDrop)
                WeatherStat("Pressure", "${weather.pressure} hPa", Icons.Default.Speed)
                WeatherStat("Wind", "${String.format("%.1f", weather.windSpeed)} m/s", Icons.Default.Air)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Updated: ${DateUtils.timeAgo(weather.lastUpdated)}",
                fontSize = 11.sp,
                color = OnSurfaceDark.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun WeatherStat(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = PrimaryBlue.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Text(label, fontSize = 10.sp, color = OnSurfaceDark.copy(alpha = 0.6f))
    }
}

@Composable
fun AqiCard(aqi: AqiData2?) {
    val aqiColor = when {
        aqi == null -> OnSurfaceDark
        aqi.aqi <= 50 -> SafeGreen
        aqi.aqi <= 100 -> Color(0xFFB8E64A)
        aqi.aqi <= 150 -> WarningOrange
        aqi.aqi <= 200 -> Color(0xFFFF6B35)
        else -> DangerRed
    }

    MonitoringCard(
        title = "Air Quality Index",
        icon = Icons.Default.Air,
        iconColor = aqiColor,
        isLoading = aqi == null
    ) {
        if (aqi != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(aqiColor.copy(alpha = 0.15f))
                        .border(2.dp, aqiColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${aqi.aqi}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = aqiColor
                        )
                        Text("AQI", fontSize = 10.sp, color = aqiColor.copy(alpha = 0.7f))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        aqi.category,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = aqiColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        aqi.healthWarning,
                        fontSize = 12.sp,
                        color = OnSurfaceDark.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Updated: ${aqi.lastUpdated.take(16)}",
                        fontSize = 11.sp,
                        color = OnSurfaceDark.copy(alpha = 0.5f)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            // AQI Progress bar
            AqiProgressBar(aqi = aqi.aqi)
        }
    }
}

@Composable
fun AqiProgressBar(aqi: Int) {
    val progress = (aqi / 300f).coerceIn(0f, 1f)
    val color = when {
        aqi <= 50 -> SafeGreen
        aqi <= 100 -> Color(0xFFB8E64A)
        aqi <= 150 -> WarningOrange
        aqi <= 200 -> Color(0xFFFF6B35)
        else -> DangerRed
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", fontSize = 10.sp, color = OnSurfaceDark.copy(alpha = 0.4f))
            Text("150", fontSize = 10.sp, color = OnSurfaceDark.copy(alpha = 0.4f))
            Text("300+", fontSize = 10.sp, color = OnSurfaceDark.copy(alpha = 0.4f))
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(SafeGreen, Color(0xFFB8E64A), WarningOrange, Color(0xFFFF6B35), DangerRed)
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(1f - progress)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .background(SurfaceVariantDark.copy(alpha = 0.7f))
            )
        }
    }
}

@Composable
fun EarthquakeCard(earthquake: EarthquakeData?) {
    val magnitudeColor = when {
        earthquake?.strongest == null -> OnSurfaceDark
        earthquake.strongest.magnitude < 5.0 -> SafeGreen
        earthquake.strongest.magnitude < 6.0 -> WarningOrange
        else -> DangerRed
    }

    MonitoringCard(
        title = "Earthquake Activity",
        icon = Icons.Default.Terrain,
        iconColor = magnitudeColor,
        isLoading = earthquake == null
    ) {
        if (earthquake != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${earthquake.count}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (earthquake.count > 0) WarningOrange else SafeGreen
                    )
                    Text(
                        "events in 24h",
                        fontSize = 12.sp,
                        color = OnSurfaceDark.copy(alpha = 0.7f)
                    )
                }
                if (earthquake.strongest != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Strongest", fontSize = 11.sp, color = OnSurfaceDark.copy(alpha = 0.6f))
                        Text(
                            "M${String.format("%.1f", earthquake.strongest.magnitude)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = magnitudeColor
                        )
                    }
                }
            }

            if (earthquake.strongest != null) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = OutlineDark)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = magnitudeColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Column {
                        Text(
                            earthquake.strongest.location,
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            DateUtils.formatEpochMillis(earthquake.strongest.time),
                            fontSize = 11.sp,
                            color = OnSurfaceDark.copy(alpha = 0.6f)
                        )
                    }
                }
            } else if (earthquake.count == 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "✅ No significant earthquakes in the past 24 hours",
                    fontSize = 13.sp,
                    color = SafeGreen.copy(alpha = 0.85f)
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Updated: ${DateUtils.timeAgo(earthquake.lastUpdated)}",
                fontSize = 11.sp,
                color = OnSurfaceDark.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SystemStatusCard(
    lastRefresh: Long,
    isLoading: Boolean,
    alertState: AlertState
) {
    MonitoringCard(
        title = "System Status",
        icon = Icons.Default.Monitor,
        iconColor = SafeGreen
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatusItem(
                label = "Monitoring",
                value = "Active",
                color = SafeGreen,
                icon = "🟢"
            )
            StatusItem(
                label = "API Status",
                value = if (isLoading) "Updating..." else "Online",
                color = if (isLoading) WarningOrange else SafeGreen,
                icon = if (isLoading) "🟡" else "🟢"
            )
            StatusItem(
                label = "Alert Level",
                value = alertState.level.name,
                color = when (alertState.level) {
                    AlertLevel.SAFE -> SafeGreen
                    AlertLevel.WARNING -> WarningOrange
                    AlertLevel.DANGER -> DangerRed
                },
                icon = when (alertState.level) {
                    AlertLevel.SAFE -> "🟢"
                    AlertLevel.WARNING -> "🟡"
                    AlertLevel.DANGER -> "🔴"
                }
            )
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = OutlineDark)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Schedule,
                null,
                tint = OnSurfaceDark.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (lastRefresh > 0) "Last refresh: ${DateUtils.timeAgo(lastRefresh)}"
                else "Waiting for first refresh...",
                fontSize = 12.sp,
                color = OnSurfaceDark.copy(alpha = 0.6f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Sync,
                null,
                tint = OnSurfaceDark.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Auto-refresh every 15 minutes via WorkManager",
                fontSize = 12.sp,
                color = OnSurfaceDark.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun StatusItem(label: String, value: String, color: Color, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            fontSize = 10.sp,
            color = OnSurfaceDark.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun MonitoringCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    isLoading: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, OutlineDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = iconColor,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                }
            } else {
                content()
            }
        }
    }
}
