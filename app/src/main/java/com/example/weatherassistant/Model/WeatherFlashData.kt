package com.example.weatherassistant.Model

import com.mapbox.mapboxsdk.geometry.LatLng

data class WeatherFlashData (
    val locationName: String? = null,
    val latLng: LatLng? = null,
    val temperature_2m: Double,
    val rain: Double
)