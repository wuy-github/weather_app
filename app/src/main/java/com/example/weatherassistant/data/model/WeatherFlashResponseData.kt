package com.example.weatherassistant.data.model

data class WeatherFlashResponseData (
    val latitude: Double,
    val longitude: Double,
    val current: CurrentData,
    val minutely_15: Minutely15Data
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