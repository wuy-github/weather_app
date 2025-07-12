package com.example.weatherassistant.data.remote

import android.util.Log

object LocationHelper {
    // Reverse Coordinates to location name
    suspend fun getLocationNameFromCoordinate(lat: Double, lon: Double): String? {
        return try {
            val response = RetrofitNominatimInstance.apiService.getAddressFromCoordinates(lat = lat, lon = lon)
            Log.d("ResolveDebug", "✅ Resolved name in GetLocationNameFromCoordinate = ${response.displayName}")
            response.displayName
        }catch (e: Exception) {
            Log.e("Nominatim", "❌ Nominatim faild to fetch LocationName : ${e.message}", e)
            null
        }
    }

    fun parseLatLon(location: String): Pair<Double, Double>? {
        val regex = Regex("^\\s*(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)\\s*$")
        val matchResult = regex.find(location)

        return matchResult?.let {
            val (latStr, _, lonStr, _) = matchResult.destructured
            Log.d("ResolveDebug", "✅ parse lat lon result: ${latStr}, ${lonStr}")
            Pair(latStr.toDouble(), lonStr.toDouble())
        }
    }

    suspend fun resolveLocationName(location: String): String {
        val locationResult = parseLatLon(location)
        return if(locationResult == null) location else {
            Log.d("ResolveDebug", "✅ locationResult after parsed: ${locationResult.toString()}")
            getLocationNameFromCoordinate(locationResult.first, locationResult.second) ?: location
        }
    }
}