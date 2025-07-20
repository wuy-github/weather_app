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
import com.example.weatherassistant.Model.WeatherFlashData
import com.example.weatherassistant.data.model.Minutely15Data
import com.example.weatherassistant.data.model.WeatherFlashResponseData
import com.example.weatherassistant.data.remote.RetrofitWeatherFlashInstance
import com.example.weatherassistant.data.repository.WeatherFlashDataRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.apply
import kotlin.collections.indices
import kotlin.collections.sumOf
import kotlin.ranges.until

class WeatherFlashViewModel( private val weatherDataRepository: WeatherFlashDataRepository) : ViewModel() {
    private val _wholeResponseData = MutableStateFlow<WeatherFlashResponseData?>(null)
    val wholeResponseData: StateFlow<WeatherFlashResponseData?> = _wholeResponseData

    private val _weatherFlashData = MutableStateFlow<WeatherFlashData?>(null)
    val weatherFlashData: StateFlow<WeatherFlashData?> = _weatherFlashData

    private val _rainForecast2Hours = MutableStateFlow<Double?>(null)
    val rainForecast2Hours: StateFlow<Double?> = _rainForecast2Hours

    private val openMeteoService = RetrofitWeatherFlashInstance.apiOpenMeteoService

    fun fetchData(lat: Double, lon: Double) {
        _weatherFlashData.value = null
        _rainForecast2Hours.value = null

        viewModelScope.launch {
            try {
                val response = openMeteoService.getWeatherFlashData(lat, lon)
                val googleLatLng = LatLng(response.latitude, response.longitude)

                // Save HourlyData Into Prefs:
                response.hourly?.let { data ->
                    weatherDataRepository.saveHourlyDataToPrefs(data)
                    Log.d("WeatherFlashViewModel", "🤩🤩🤩 Found hourlyData that fetched from Open Meteo!!!")
                }

                val weatherData = WeatherFlashData(
                    latLng = googleLatLng,
                    temperature_2m = response.current.temperature_2m,
                    rain = response.current.rain
                )
                _weatherFlashData.value = weatherData

                Log.d("WeatherFlashViewModel", "\uD83E\uDD29\uD83E\uDD29 Fetch Flash Data Done!!! - ${weatherData.latLng}")
                val totalRain = calculateRainForecastInNext2Hours(
                    response.minutely_15,
                    response.current.time,
                    "Asia/Bangkok" // có thể dùng response.timezone nếu có
                )
                _rainForecast2Hours.value = totalRain

            } catch (e: Exception) {
                Log.e("WeatherFlashViewModel", "Lỗi khi gọi API: ${e.message}", e)
            }
        }
    }

    private fun calculateRainForecastInNext2Hours(
        minutely15Data: Minutely15Data,
        apiCurrentTimeString: String,
        timezoneId: String?
    ): Double {
        val startIndex = calculateStartTime(apiCurrentTimeString, timezoneId)
        if (startIndex == -1) {
            Log.e("WeatherViewModel", "Không thể tính toán thời gian bắt đầu")
            return 0.0
        }

        val rainList = minutely15Data.rain
        return (0 until 8).sumOf { i ->
            val idx = startIndex + i
            if (idx in rainList.indices) rainList[idx] else 0.0
        }
    }

    private fun calculateStartTime(timeString: String, timezoneId: String?): Int {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()).apply {
                timeZone = try {
                    TimeZone.getTimeZone(timezoneId ?: "UTC")
                } catch (_: Exception) {
                    TimeZone.getTimeZone("UTC")
                }
            }

            val date = formatter.parse(timeString) ?: return -1
            val cal = Calendar.getInstance(formatter.timeZone).apply { time = date }

            (cal.get(Calendar.HOUR_OF_DAY) * 4) + (cal.get(Calendar.MINUTE) / 15)
        } catch (e: Exception) {
            Log.e("TimeCalc", "Lỗi xử lý thời gian: ${e.message}", e)
            -1
        }
    }

    fun clearWeatherData() {
        _weatherFlashData.value = null
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


}
