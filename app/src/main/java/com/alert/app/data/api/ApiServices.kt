package com.alert.app.data.api

import com.alert.app.data.models.AqiResponse
import com.alert.app.data.models.EarthquakeResponse
import com.alert.app.data.models.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>
}

interface AqiApiService {
    @GET
    suspend fun getAqi(@Url url: String): Response<AqiResponse>
}

interface EarthquakeApiService {
    @GET("earthquakes/feed/v1.0/summary/significant_day.geojson")
    suspend fun getEarthquakes(): Response<EarthquakeResponse>
}
