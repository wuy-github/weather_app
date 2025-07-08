package com.example.weatherassistant.data.remote

import com.example.weatherassistant.data.model.WikipediaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WikipediaApiService {
    @GET("api.php")
    suspend fun getNearbyPlaces(
        @Query("action") action: String = "query",
        @Query("list") list: String = "geosearch",
        @Query("gscoord") coordinates: String, // Ví dụ: "10.76|106.66"
        @Query("gsradius") radius: Int = 10000, // Bán kính 10km
        @Query("gslimit") limit: Int = 10,
        @Query("format") format: String = "json"
    ): WikipediaResponse
}