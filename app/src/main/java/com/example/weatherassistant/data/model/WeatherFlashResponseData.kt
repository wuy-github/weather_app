package com.example.weatherassistant.data.model

data class WeatherFlashResponseData (
    val latitude: Double,
    val longitude: Double,
    val current: CurrentData,
    val minutely_15: Minutely15Data,
    val hourly: HourlyData
)

    data class CurrentData (
        val time: String,
        val temperature_2m: Double,
        val precipitation: Double,
        val rain: Double,
        val windspeed_10m: Double
    )

    data class Minutely15Data(
        val time: List<String>,
        val temperature_2m: List<Double>,
        val precipitation: List<Double>,
        val rain: List<Double>
    )

    data class HourlyData(
        val temperature_2m: List<Double>,
        val precipitation: List<Double>,
        val rain: List<Double>,
        val cloud_cover: List<Int>,
        val wind_speed_10m: List<Double>,
        val wind_direction_10m: List<Int>,
        val wind_gusts_10m: List<Double>,
        val weather_code: List<Int>
    )