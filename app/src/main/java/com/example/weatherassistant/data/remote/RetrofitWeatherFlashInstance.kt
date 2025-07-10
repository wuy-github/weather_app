package com.example.weatherassistant.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

object RetrofitWeatherFlashInstance {
    private const val OM_BASE_URL = "https://api.open-meteo.com/"
    val apiOpenMeteoService: ApiOpenMeteoService by lazy {
        Retrofit.Builder()
            .baseUrl(OM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiOpenMeteoService::class.java)
    }
}