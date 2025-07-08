package com.example.weatherassistant.data.remote

import com.example.weatherassistant.data.model.WeatherApiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApiService {
    //Get today Weather
    @GET("VisualCrossingWebServices/rest/services/timeline/{location}")
    suspend fun getTodayWeather(
        @Path("location") location: String,
        @Query("unitGroup") unitGroup: String = "metric",
       // @Query("include") include: String = "hours",
        @Query("key") key: String
    ): WeatherApiResponse

    @GET("VisualCrossingWebServices/rest/services/timeline/{location}")
    suspend fun getDailyForcast(
        @Path("location") location: String,
        @Query("startDate") startDate: String, // yyyy-mm-dd
        @Query("endDate") endDate: String,
        @Query("unitGroup") unitGroup: String = "metric",
        @Query("include") include: String = "days",
        @Query("key") key: String
    ): WeatherApiResponse
}