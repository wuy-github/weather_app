package com.example.weatherassistant.data.remote

import com.example.weatherassistant.data.model.NominatimResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ReverseGeocodingService {
    @GET("reverse")
    suspend fun getAddressFromCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json"
    ): NominatimResponse
}