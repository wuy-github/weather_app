package com.example.weatherassistant.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherassistant.data.repository.WeatherFlashDataRepository
import com.example.weatherassistant.viewmodel.WeatherFlashViewModel

class WeatherFlashViewModelFactory(
    private val application: Application,
    private val repository: WeatherFlashDataRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherFlashViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return WeatherFlashViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}