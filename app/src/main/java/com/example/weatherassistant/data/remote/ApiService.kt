package com.example.weatherassistant.data.remote

import com.example.weatherassistant.data.model.WeatherFlashResponseData
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiOpenMeteoService {
    @GET("v1/forecast")
    suspend fun getWeatherFlashData(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,is_day,precipitation,rain,wind_speed_10m",
        @Query("minutely_15") minutely15: String = "temperature_2m,precipitation,rain",
        @Query("timezone") timezone: String = "auto"
    ): WeatherFlashResponseData
}
