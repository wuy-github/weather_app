package com.example.weatherassistant.data.remote

import android.R.attr.level
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object HttpClientProvider {
    fun defaultClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC // Chọn BODY nếu muốn xem JSON
            })
            .build()
    }

    fun nominatimClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "WeatherAssistance/1.0 (tranbaodai1312@gmail.com)")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor( HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            } )
            .build()
    }
}