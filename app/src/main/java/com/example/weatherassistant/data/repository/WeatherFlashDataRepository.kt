package com.example.weatherassistant.data.repository

import android.content.Context
import com.example.weatherassistant.data.model.HourlyData
import com.google.gson.Gson

class WeatherFlashDataRepository(private val context: Context) {
    fun saveHourlyDataToPrefs(hourlyData: HourlyData) {
        val json = Gson().toJson(hourlyData)
        context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
            .edit()
            .putString("hourly_data", json)
            .apply()
    }

    fun readHourlyDataFromPrefs(): HourlyData? {
        val json = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
            .getString("hourly_data", null)
        return json?.let {
            Gson().fromJson(it, HourlyData::class.java)
        }
    }
}