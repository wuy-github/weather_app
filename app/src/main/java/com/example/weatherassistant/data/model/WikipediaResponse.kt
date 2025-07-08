package com.example.weatherassistant.data.model

import com.google.gson.annotations.SerializedName

// Lớp gốc của JSON response
data class WikipediaResponse(
    val query: Query
)

// Lớp chứa danh sách các địa điểm
data class Query(
    @SerializedName("geosearch")
    val geoSearch: List<GeoSearchItem>
)

// Lớp chứa thông tin của một địa điểm
data class GeoSearchItem(
    @SerializedName("pageid")
    val pageId: Int,
    val title: String,
    val lat: Double,
    val lon: Double,
    val dist: Double
)