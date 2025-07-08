package com.example.weatherassistant.data.repository

import com.example.weatherassistant.data.model.GeoSearchItem
import com.example.weatherassistant.data.remote.RetrofitWikipediaInstance

class WikipediaRepository {

    private val wikipediaService = RetrofitWikipediaInstance.apiService

    /**
     * Lấy danh sách các địa điểm gần vị trí cho trước từ Wikipedia API.
     * @param lat Vĩ độ
     * @param lon Kinh độ
     * @return Một danh sách các địa điểm (GeoSearchItem) hoặc null nếu có lỗi.
     */
    suspend fun getNearbyPlaces(lat: Double, lon: Double): List<GeoSearchItem>? {
        return try {
            // Định dạng tọa độ thành "lat|lon" theo yêu cầu của API
            val coordinates = "$lat|$lon"
            val response = wikipediaService.getNearbyPlaces(coordinates = coordinates)
            response.query.geoSearch
        } catch (e: Exception) {
            // Nếu có lỗi mạng hoặc lỗi phân tích JSON, trả về null
            e.printStackTrace()
            null
        }
    }
}