package com.example.weatherassistant.Model

import com.google.android.gms.maps.model.LatLng

data class WeatherFlashData (
    val locationName: String? = null,
    val latLng: LatLng? = null,
    val temperature_2m: Double,
    val rain: Double
)