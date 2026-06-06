package com.alert.app.data.repository

import com.alert.app.BuildConfig
import com.alert.app.data.api.ApiClient
import com.alert.app.data.models.*

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class AlertRepository {

    suspend fun fetchWeather(city: String): Result<WeatherData> {
        return try {
            val response = ApiClient.weatherApiService.getWeather(
                city = city,
                apiKey = BuildConfig.OPENWEATHER_API_KEY
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val weatherData = WeatherData(
                    city = body.cityName,
                    temperature = body.main.temp,
                    feelsLike = body.main.feelsLike,
                    humidity = body.main.humidity,
                    pressure = body.main.pressure,
                    windSpeed = body.wind.speed,
                    condition = body.weather.firstOrNull()?.main ?: "Unknown",
                    description = body.weather.firstOrNull()?.description ?: "Unknown",
                    lastUpdated = System.currentTimeMillis()
                )
                Result.Success(weatherData)
            } else {
                Result.Error("Weather API error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}", e)
        }
    }

    suspend fun fetchAqi(city: String): Result<AqiData2> {
        return try {
            val url = "https://api.waqi.info/feed/${city.lowercase()}/?token=${BuildConfig.WAQI_TOKEN}"
            val response = ApiClient.aqiApiService.getAqi(url)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.status == "ok" && body.data != null) {
                    val aqi = body.data.aqi
                    val aqiData = AqiData2(
                        aqi = aqi,
                        category = getAqiCategory(aqi),
                        healthWarning = getAqiHealthWarning(aqi),
                        lastUpdated = body.data.time?.timestamp ?: "Unknown"
                    )
                    Result.Success(aqiData)
                } else {
                    Result.Error("AQI data not available for $city. Status: ${body.status}")
                }
            } else {
                Result.Error("AQI API error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}", e)
        }
    }

    suspend fun fetchEarthquakes(): Result<EarthquakeData> {
        return try {
            val response = ApiClient.earthquakeApiService.getEarthquakes()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val strongest = body.features
                    .filter { it.properties.magnitude != null }
                    .maxByOrNull { it.properties.magnitude!! }

                val earthquakeData = EarthquakeData(
                    count = body.features.size,
                    strongest = strongest?.let {
                        EarthquakeEvent(
                            magnitude = it.properties.magnitude ?: 0.0,
                            location = it.properties.place ?: "Unknown location",
                            time = it.properties.time ?: 0L
                        )
                    },
                    lastUpdated = System.currentTimeMillis()
                )
                Result.Success(earthquakeData)
            } else {
                Result.Error("Earthquake API error: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}", e)
        }
    }

    fun evaluateAlertLevel(
        weather: WeatherData?,
        aqi: AqiData2?,
        earthquake: EarthquakeData?,
        aqiThreshold: Int = 100,
        tempThreshold: Double = 40.0
    ): AlertState {
        var level = AlertLevel.SAFE
        var weatherAlert: String? = null
        var aqiAlert: String? = null
        var earthquakeAlert: String? = null

        // Temperature check
        weather?.let {
            when {
                it.temperature > 45.0 -> {
                    level = AlertLevel.DANGER
                    weatherAlert = "DANGER: Extreme heat! Temperature is ${String.format("%.1f", it.temperature)}°C"
                }
                it.temperature >= tempThreshold || it.temperature >= 40.0 -> {
                    if (level != AlertLevel.DANGER) level = AlertLevel.WARNING
                    weatherAlert = "WARNING: High temperature! ${String.format("%.1f", it.temperature)}°C"
                }
            }
        }

        // AQI check
        aqi?.let {
            when {
                it.aqi > 200 -> {
                    level = AlertLevel.DANGER
                    aqiAlert = "DANGER: Hazardous air quality! AQI ${it.aqi}"
                }
                it.aqi >= aqiThreshold || it.aqi >= 100 -> {
                    if (level != AlertLevel.DANGER) level = AlertLevel.WARNING
                    aqiAlert = "WARNING: Poor air quality! AQI ${it.aqi}"
                }
            }
        }

        // Earthquake check
        earthquake?.strongest?.let {
            when {
                it.magnitude > 6.0 -> {
                    level = AlertLevel.DANGER
                    earthquakeAlert = "DANGER: Major earthquake M${String.format("%.1f", it.magnitude)} detected!"
                }
                it.magnitude >= 5.0 -> {
                    if (level != AlertLevel.DANGER) level = AlertLevel.WARNING
                    earthquakeAlert = "WARNING: Significant earthquake M${String.format("%.1f", it.magnitude)} detected!"
                }
            }
        }

        return AlertState(
            level = level,
            weatherAlert = weatherAlert,
            aqiAlert = aqiAlert,
            earthquakeAlert = earthquakeAlert
        )
    }
}
