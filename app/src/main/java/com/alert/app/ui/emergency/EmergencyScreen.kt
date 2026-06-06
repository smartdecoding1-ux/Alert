package com.alert.app.ui.emergency

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alert.app.data.models.AlertState
import com.alert.app.data.models.EarthquakeData
import com.alert.app.data.models.WeatherData
import com.alert.app.data.models.AqiData2
import com.alert.app.ui.theme.*

@Composable
fun EmergencyScreen(
    alertState: AlertState,
    weather: WeatherData?,
    aqi: AqiData2?,
    earthquake: EarthquakeData?,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emergency")

    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF3D0000).copy(alpha = flashAlpha),
                        Color(0xFF1A0000),
                        Color(0xFF0D0000)
                    )
                )
            )
    ) {
        // Pulsing background overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DangerRed.copy(alpha = flashAlpha * 0.15f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            // Flashing warning icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(DangerRed.copy(alpha = flashAlpha * 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(DangerRed.copy(alpha = flashAlpha * 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚨", fontSize = 48.sp)
                }
            }

            // Emergency title
            Text(
                "EMERGENCY\nALERT",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DangerRed.copy(alpha = flashAlpha.coerceAtLeast(0.7f)),
                textAlign = TextAlign.Center,
                letterSpacing = 4.sp,
                lineHeight = 42.sp
            )

            Text(
                "DANGEROUS ENVIRONMENTAL CONDITIONS\nDETECTED IN YOUR AREA",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp,
                lineHeight = 20.sp
            )

            // Alert details
            val alerts = listOfNotNull(
                alertState.weatherAlert,
                alertState.aqiAlert,
                alertState.earthquakeAlert
            )

            if (alerts.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2D0000).copy(alpha = 0.9f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, DangerRed.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "ACTIVE ALERTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DangerRed.copy(alpha = 0.8f),
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        alerts.forEach { alert ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("⚠ ", fontSize = 14.sp)
                                Text(
                                    alert,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Emergency instructions
            EmergencyInstructionsCard(
                hasHeatAlert = alertState.weatherAlert != null,
                hasAqiAlert = alertState.aqiAlert != null,
                hasEarthquakeAlert = alertState.earthquakeAlert != null
            )

            // Current readings
            CurrentReadingsCard(weather = weather, aqi = aqi, earthquake = earthquake)

            // Dismiss button
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A2E),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "ACKNOWLEDGE & DISMISS",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Text(
                "Tap dismiss only if you have taken necessary precautions",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun EmergencyInstructionsCard(
    hasHeatAlert: Boolean,
    hasAqiAlert: Boolean,
    hasEarthquakeAlert: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1F0D).copy(alpha = 0.9f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, SafeGreen.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📋", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "EMERGENCY INSTRUCTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SafeGreen,
                    letterSpacing = 2.sp
                )
            }
            Spacer(Modifier.height(12.dp))

            if (hasHeatAlert) {
                InstructionSection("🌡️ Extreme Heat", listOf(
                    "Stay indoors with air conditioning",
                    "Drink plenty of water — stay hydrated",
                    "Avoid outdoor activities during peak hours",
                    "Check on elderly and vulnerable neighbors",
                    "Close curtains and blinds to block sunlight"
                ))
                Spacer(Modifier.height(8.dp))
            }
            if (hasAqiAlert) {
                InstructionSection("😷 Poor Air Quality", listOf(
                    "Wear N95/KN95 mask when going outside",
                    "Keep windows and doors closed",
                    "Use air purifier indoors if available",
                    "Avoid strenuous outdoor activities",
                    "Seek medical help if breathing problems occur"
                ))
                Spacer(Modifier.height(8.dp))
            }
            if (hasEarthquakeAlert) {
                InstructionSection("🌍 Earthquake Activity", listOf(
                    "Drop, Cover, and Hold On",
                    "Stay away from windows and heavy furniture",
                    "Move to open areas away from buildings",
                    "Be prepared for aftershocks",
                    "Have emergency kit ready (water, food, first aid)"
                ))
            }
        }
    }
}

@Composable
fun InstructionSection(title: String, instructions: List<String>) {
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    Spacer(Modifier.height(6.dp))
    instructions.forEach { instruction ->
        Row(
            modifier = Modifier.padding(vertical = 2.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("  •  ", fontSize = 12.sp, color = SafeGreen.copy(alpha = 0.7f))
            Text(instruction, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f), lineHeight = 17.sp)
        }
    }
}

@Composable
fun CurrentReadingsCard(
    weather: WeatherData?,
    aqi: AqiData2?,
    earthquake: EarthquakeData?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.8f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "CURRENT READINGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryBlue.copy(alpha = 0.8f),
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weather?.let {
                    ReadingItem("🌡️", "${String.format("%.1f", it.temperature)}°C", "Temperature")
                }
                aqi?.let {
                    ReadingItem("💨", "${it.aqi}", "AQI")
                }
                earthquake?.strongest?.let {
                    ReadingItem("🌍", "M${String.format("%.1f", it.magnitude)}", "EQ Magnitude")
                } ?: ReadingItem("🌍", "None", "Earthquake")
            }
        }
    }
}

@Composable
fun ReadingItem(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 10.sp, color = OnSurfaceDark.copy(alpha = 0.6f))
    }
}
