package com.example.weatherassistant.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherassistant.Model.WeatherData
import com.example.weatherassistant.UIState.WeatherUIState
import com.example.weatherassistant.data.model.GeoSearchItem
import com.example.weatherassistant.data.model.WeatherApiResponse
import com.example.weatherassistant.data.model.WeatherDay
import com.example.weatherassistant.data.remote.LocationHelper.resolveLocationName
import com.example.weatherassistant.data.remote.RetrofitInstance
import com.example.weatherassistant.data.repository.WikipediaRepository
import com.example.weatherassistant.data.repository.UserPreferencesRepository
import com.example.weatherassistant.views.components.WhatTheDay
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

// 👇 Cấu trúc class đúng bắt đầu từ đây
class WeatherDataViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val wikipediaRepository: WikipediaRepository

) : ViewModel() {

    private val _notification = MutableStateFlow<String?>(null)
    val notification: StateFlow<String?> = _notification

    private val _uiState = MutableStateFlow<WeatherUIState>(WeatherUIState.Empty)
    val uiState: StateFlow<WeatherUIState> = _uiState

    private val _wholeResponseData = MutableStateFlow<WeatherApiResponse?>(null)
    val wholeResponseData: StateFlow<WeatherApiResponse?> = _wholeResponseData

    val listDaysData: List<WeatherDay>
        get() = _wholeResponseData.value?.days ?: emptyList()

    val searchHistory = userPreferencesRepository.searchHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _nearbyPlaces = MutableStateFlow<List<GeoSearchItem>>(emptyList())
    val nearbyPlaces: StateFlow<List<GeoSearchItem>> = _nearbyPlaces

    fun fetchWeatherFor(location: String) {
        viewModelScope.launch {
            if (_uiState.value !is WeatherUIState.Success) _uiState.value = WeatherUIState.Loading

            Log.d("fetchData", "✅ Calling fetchWeatherFor for: $location")
            delay(1500)

            try {
                val apiKey = "QLB4PA4P58FEG5B3E3M2CZQ7P"
                val rawResponse: WeatherApiResponse = RetrofitInstance.apiService.getTodayWeather(location, key = apiKey)
                val resolvedAddress = resolveLocationName(rawResponse.address)

                _wholeResponseData.value = rawResponse.copy(address = resolvedAddress)

                // Make sure the address to be Location name:
                    Log.d("ChangedLocationName", "✅ Resolve = ${_wholeResponseData.value?.address}")

                _uiState.value = WeatherUIState.Success
                Log.d("fetchData", "✅ Successfully fetched data for $location \n ${RetrofitInstance.apiService.getTodayWeather(location, key = apiKey)}")

                fetchNearbyPlaces(rawResponse.latitude, rawResponse.longitude)
                if (location.isNotBlank() && !location.contains(",")) {
                    userPreferencesRepository.addLocationToHistory(location)
                    Log.d("DataStore", "✅ Saved '$location' to history.")
                }


            } catch (e: Exception) {
                showNotification("❌ Địa điểm bạn nhập không hợp lệ ❌")
                Log.e("fetchData", "❌ Failed to fetch data: ${e.message}", e)
            }
        }
    }

    fun fetchNearbyPlaces(lat: Double?, lon: Double?) {
        viewModelScope.launch {
            val places = if (lat != null && lon != null) wikipediaRepository.getNearbyPlaces(lat, lon) else null
            if (places != null) {
                _nearbyPlaces.value = places
                Log.d("WikipediaAPI", "✅ Found ${places.size} nearby places.")
            } else {
                Log.e("WikipediaAPI", "❌ Failed to fetch nearby places.")
            }
        }
    }

    fun getCurrentLocation(context: Context, onLocationReceive: (Location?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            numUpdates = 1
        }

        val locationCallBack = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                p0.lastLocation?.let { location ->
                    onLocationReceive(location)
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onLocationReceive(null)
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallBack,
            Looper.getMainLooper()
        )
    }

    // Perform Current Location Weather Data Fetch:
    fun fetchCurrentLocationWeatherData(context: Context) {
        getCurrentLocation(context) { location ->
            if (location == null) {
                setError("Không lấy được vị trí thiết bị")
                Log.e("CurrentLocation", "❌ Không lấy được vị trí hiện tại")
            } else {
                // Gọi cả hai hàm fetch khi có vị trí
                val lat = location.latitude
                val lon = location.longitude
                fetchWeatherFor("$lat,$lon")
                fetchNearbyPlaces(lat, lon)
            }
        }
    }

    fun setError(message: String) {
        _uiState.value = WeatherUIState.Error(message)
    }

    fun showNotification(message: String?) {
        _notification.value = message
        viewModelScope.launch {
            delay(3000)
            _notification.value = null
        }
    }

    fun getTodayIndex(): Int? {
        val today = LocalDate.now()
        return listDaysData.indexOfFirst { it.datetime == today.toString() }.takeIf { it != -1 }
    }

    fun getWeatherDataByDay(dateIndex: Int): WeatherData {
        val dayData = listDaysData[dateIndex]

        return WeatherData(
            locationName = _wholeResponseData.value?.address ?: "",
            location = _wholeResponseData.value?.address ?: "",
            condition = dayData.icon,
            currentTemp = dayData.temp,
            maxTemp = dayData.tempmax,
            minTemp = dayData.tempmin,
            dayOfWeek = WhatTheDay(LocalDate.parse(dayData.datetime)),
            date = LocalDate.parse(dayData.datetime),
            humidity = dayData.humidity,
            pressure = dayData.pressure,
            windSpeed = dayData.windspeed,
            sunset = dayData.sunset,
            sunrise = dayData.sunrise,
            uvIndex = dayData.uvindex)
    }

    fun getWeatherDataByHour(dateIndex: Int, hour: Int): WeatherData {
        val dayData = listDaysData[dateIndex]
        val hourData = dayData.hours[hour]

        return WeatherData(
            locationName = _wholeResponseData.value?.address ?: "",
            location = _wholeResponseData.value?.address ?: "",
            condition = hourData.icon,
            currentTemp = hourData.temp,
            maxTemp = dayData.tempmax,
            minTemp = dayData.tempmin,
            dayOfWeek = WhatTheDay(LocalDate.parse(dayData.datetime)),
            date = LocalDate.parse(dayData.datetime),
            humidity = hourData.humidity,
            pressure = hourData.pressure,
            windSpeed = hourData.windspeed,
            sunset = dayData.sunset,
            sunrise = dayData.sunrise,
            uvIndex = hourData.uvindex)
    }
    //hàm xóa lịch sử tìm kiếm
    fun clearSearchHistory(){
        viewModelScope.launch {
            userPreferencesRepository.clearHistory()
        }
    }
}

