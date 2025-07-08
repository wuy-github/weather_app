package com.example.weatherassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherassistant.data.repository.UserPreferencesRepository
import com.example.weatherassistant.data.repository.WikipediaRepository

// Thêm wikipediaRepository vào constructor
class WeatherDataViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val wikipediaRepository: WikipediaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Truyền cả hai repository vào ViewModel
            return WeatherDataViewModel(userPreferencesRepository, wikipediaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}