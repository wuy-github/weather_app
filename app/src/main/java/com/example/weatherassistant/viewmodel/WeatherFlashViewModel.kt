package com.example.weatherassistant.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherassistant.Model.WeatherFlashData
import com.example.weatherassistant.data.model.Minutely15Data
import com.example.weatherassistant.data.remote.RetrofitWeatherFlashInstance
import com.mapbox.mapboxsdk.geometry.LatLng
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

class WeatherFlashViewModel : ViewModel() {

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

                val weatherData = WeatherFlashData(
                    latLng = LatLng(response.latitude, response.longitude),
                    temperature_2m = response.current.temperature_2m,
                    rain = response.current.rain
                )
                _weatherFlashData.value = weatherData

                val totalRain = calculateRainForecastInNext2Hours(
                    response.minutely_15,
                    response.current.time,
                    "Asia/Bangkok" // có thể dùng response.timezone nếu có
                )
                _rainForecast2Hours.value = totalRain

            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Lỗi khi gọi API: ${e.message}", e)
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

}
