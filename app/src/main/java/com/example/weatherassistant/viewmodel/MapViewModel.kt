package com.example.weatherassistant.viewmodel

import android.app.Application
import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherassistant.data.repository.MapRepository
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngQuad
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.RasterLayer
import com.mapbox.mapboxsdk.style.sources.ImageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val mapRepository = MapRepository(application)

    var layerType = mutableStateOf("none")
    var mapStyle = mutableStateOf<Style?>(null)

    fun updateWeatherLayer(lat: Double, lon: Double, zoom: Int) {
        val currentStyle = mapStyle.value ?: return

        // Gọi hàm remove cục bộ
        removeWeatherLayer(currentStyle)

        if (layerType.value == "none") return

        viewModelScope.launch(Dispatchers.IO) {
            val tilesToDownload = mapRepository.getTilesToDownload(lat, lon, zoom, 3, 3)

            mapRepository.fetchAndCacheTiles(layerType.value, tilesToDownload, zoom) { file, corners ->
                val (topLeft, bottomRight) = corners
                val quad = LatLngQuad(
                    LatLng(topLeft.latitude, topLeft.longitude),
                    LatLng(topLeft.latitude, bottomRight.longitude),
                    LatLng(bottomRight.latitude, bottomRight.longitude),
                    LatLng(bottomRight.latitude, topLeft.longitude)
                )

                viewModelScope.launch(Dispatchers.Main) {
                    // Gọi hàm show cục bộ
                    showWeatherImageOverlay(currentStyle, file, quad)
                }
            }
        }
    }

    // 👇 THÊM HÀM NÀY VÀO
    private fun showWeatherImageOverlay(style: Style, file: File, quad: LatLngQuad) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return

        // Xóa source cũ nếu có để tránh lỗi
        style.getSource("weather-overlay")?.let { style.removeSource(it) }

        val imageSource = ImageSource("weather-overlay", quad, bitmap)
        style.addSource(imageSource)

        val imageLayer = RasterLayer("weather-layer", "weather-overlay").apply {
            setProperties(PropertyFactory.rasterOpacity(0.7f))
        }
        style.addLayer(imageLayer)
    }

    // 👇 VÀ THÊM CẢ HÀM NÀY
    private fun removeWeatherLayer(style: Style) {
        style.getLayer("weather-layer")?.let { style.removeLayer(it) }
        style.getSource("weather-overlay")?.let { style.removeSource(it) }
    }
}