package com.example.weatherassistant.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object RetrofitNominatimInstance {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    val apiService : ReverseGeocodingService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(HttpClientProvider.nominatimClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReverseGeocodingService::class.java)
    }
}