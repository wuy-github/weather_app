package com.example.weatherassistant.views.components

import com.example.weatherassistant.Model.WeatherData
import java.time.LocalDate
import kotlin.String

val ErrorData = WeatherData(
    locationName  = "Error",
    location  = "Error",
    condition  = "error",
    currentTemp  = 0.0,
    maxTemp  = 0.0,
    minTemp  = 0.0,
    dayOfWeek  = "Error",
    date = LocalDate.parse("01/01/1900"),
    humidity  = 0.0,
    pressure  = 0.0,
    windSpeed  = 0.0,
    sunrise  = "Error",
    sunset  = "Error",
    uvIndex = 0
)