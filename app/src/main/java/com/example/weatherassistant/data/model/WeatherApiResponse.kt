package com.example.weatherassistant.data.model


//@JsonClass(generateAdapter = true)
data class WeatherApiResponse (
    val timezone: String,
    val address: String,
    val days: List<WeatherDay>
)

//@JsonClass(generateAdapter = true)
data class WeatherDay(
    val datetime: String,
    val temp: Double,
    val tempmax: Double,
    val tempmin: Double,
    val icon: String,
    val humidity: Double,
    val windspeed: Double,
    val pressure: Double,
    val sunrise: String,
    val sunset: String,
    val uvindex: Int,

    val hours: List<WeatherHour>
)

//@JsonClass(generateAdapter = true)
data class WeatherHour(
    val datetime: String,
    val temp: Double,
    val tempmax: Double,
    val tempmin: Double,
    val icon: String,
    val humidity: Double,
    val windspeed: Double,
    val pressure: Double,
    val uvindex: Int
    )