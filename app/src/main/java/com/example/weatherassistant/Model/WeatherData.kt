package com.example.weatherassistant.Model

import java.time.LocalDate

data class WeatherData (
    val locationName: String,
    val location: String,
    val condition: String,
    val currentTemp: Double,
    val maxTemp: Double,
    val minTemp: Double,
    val dayOfWeek: String,
    val date: LocalDate,
    val humidity: Double,
    val pressure: Double,
    val windSpeed: Double,
    val sunrise: String,
    val sunset: String,
    val uvIndex: Int
)
