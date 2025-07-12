package com.example.weatherassistant.data.model

import com.google.gson.annotations.SerializedName

data class NominatimResponse(
    @SerializedName("display_name")
    val displayName: String
)