package com.alert.app.data.models

import com.google.gson.annotations.SerializedName

// ── Weather Models ──────────────────────────────────────────────────────────

data class WeatherResponse(
    @SerializedName("main") val main: WeatherMain,
    @SerializedName("weather") val weather: List<WeatherCondition>,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("name") val cityName: String,
    @SerializedName("dt") val timestamp: Long
)

data class WeatherMain(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("pressure") val pressure: Int
)

data class WeatherCondition(
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class Wind(
    @SerializedName("speed") val speed: Double
)

// ── AQI Models ──────────────────────────────────────────────────────────────

data class AqiResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: AqiData?
)

data class AqiData(
    @SerializedName("aqi") val aqi: Int,
    @SerializedName("time") val time: AqiTime?,
    @SerializedName("city") val city: AqiCity?
)

data class AqiTime(
    @SerializedName("s") val timestamp: String
)

data class AqiCity(
    @SerializedName("name") val name: String
)

// ── Earthquake Models ───────────────────────────────────────────────────────

data class EarthquakeResponse(
    @SerializedName("features") val features: List<EarthquakeFeature>,
    @SerializedName("metadata") val metadata: EarthquakeMetadata
)

data class EarthquakeMetadata(
    @SerializedName("count") val count: Int,
    @SerializedName("title") val title: String
)

data class EarthquakeFeature(
    @SerializedName("properties") val properties: EarthquakeProperties,
    @SerializedName("id") val id: String
)

data class EarthquakeProperties(
    @SerializedName("mag") val magnitude: Double?,
    @SerializedName("place") val place: String?,
    @SerializedName("time") val time: Long?,
    @SerializedName("title") val title: String?,
    @SerializedName("alert") val alert: String?,
    @SerializedName("tsunami") val tsunami: Int?
)

// ── App State Models ────────────────────────────────────────────────────────

data class WeatherData(
    val city: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val condition: String,
    val description: String,
    val lastUpdated: Long
)

data class AqiData2(
    val aqi: Int,
    val category: String,
    val healthWarning: String,
    val lastUpdated: String
)

data class EarthquakeData(
    val count: Int,
    val strongest: EarthquakeEvent?,
    val lastUpdated: Long
)

data class EarthquakeEvent(
    val magnitude: Double,
    val location: String,
    val time: Long
)

enum class AlertLevel { SAFE, WARNING, DANGER }

data class AlertState(
    val level: AlertLevel = AlertLevel.SAFE,
    val weatherAlert: String? = null,
    val aqiAlert: String? = null,
    val earthquakeAlert: String? = null
)

data class MonitoringState(
    val weather: WeatherData? = null,
    val aqi: AqiData2? = null,
    val earthquake: EarthquakeData? = null,
    val alertState: AlertState = AlertState(),
    val isLoading: Boolean = false,
    val lastRefresh: Long = 0L,
    val error: String? = null
)

fun getAqiCategory(aqi: Int): String = when {
    aqi <= 50 -> "Good"
    aqi <= 100 -> "Moderate"
    aqi <= 150 -> "Unhealthy for Sensitive Groups"
    aqi <= 200 -> "Unhealthy"
    aqi <= 300 -> "Very Unhealthy"
    else -> "Hazardous"
}

fun getAqiHealthWarning(aqi: Int): String = when {
    aqi <= 50 -> "Air quality is satisfactory. No health concerns."
    aqi <= 100 -> "Acceptable air quality. Sensitive individuals may experience minor issues."
    aqi <= 150 -> "Sensitive groups may experience health effects. General public unaffected."
    aqi <= 200 -> "Health effects possible for everyone. Sensitive groups: serious effects."
    aqi <= 300 -> "Health alert: everyone may experience serious health effects."
    else -> "Health warning of emergency conditions. Everyone is affected."
}
