package com.example.weatherassistant.UIState

import com.example.weatherassistant.Model.WeatherData
import com.example.weatherassistant.data.model.WeatherApiResponse

sealed class WeatherUIState {
    object Loading : WeatherUIState()
    object Success : WeatherUIState()
    data class Error(val message: String) : WeatherUIState()
    object Empty : WeatherUIState()
}

