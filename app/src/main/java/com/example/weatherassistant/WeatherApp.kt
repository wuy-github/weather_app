// File: WeatherApp.kt
package com.example.weatherassistant

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherassistant.UIState.ErrorScreen
import com.example.weatherassistant.UIState.LoadingScreen
import com.example.weatherassistant.UIState.WeatherUIState
import com.example.weatherassistant.viewmodel.WeatherDataViewModel
import com.example.weatherassistant.views.LocationHistoryScreen
import com.example.weatherassistant.views.MainScreen
import com.example.weatherassistant.views.MapScreen

@Composable
fun WeatherApp(viewModel: WeatherDataViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var hasFetched by remember { mutableStateOf(false) }

    // Logic lấy vị trí lần đầu
    LaunchedEffect(Unit) {
        if (!hasFetched) {
            viewModel.getCurrentLocation(context = context) { location ->
                if (location != null) {
                    val lat = location.latitude
                    val long = location.longitude
                    viewModel.fetchWeatherFor("$lat,$long")
                } else {
                    viewModel.setError("Không lấy được vị trí thiết bị")
                    Log.e("CurrentLocation", "❌ Không lấy được vị trí hiện tại")
                }
            }
            hasFetched = true
        }
    }

    val navController = rememberNavController()
    var currentLat = viewModel.wholeResponseData.value?.latitude ?: 60.0
    var currentLon = viewModel.wholeResponseData.value?.longitude ?: 100.0

    // Lựa chọn giao diện dựa trên trạng thái
    when (uiState) {
        is WeatherUIState.Success -> {
            NavHost(navController = navController, startDestination = "main_screen") {
                composable("main_screen") {
                    MainScreen(
                        viewModel = viewModel,
                        context = context,
                        onLocationClick = { navController.navigate("history_screen") }
                    )
                }
                composable("history_screen") {
                    LocationHistoryScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onHistoryItemClick = { location ->
                            navController.popBackStack()
                            viewModel.fetchWeatherFor(location)
                        },
                        onNearbyPlaceClick = { destination ->

                            val intentUri = Uri.parse("geo:0,0?q=${Uri.encode(destination)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, intentUri)

                            context.startActivity(mapIntent)
                        },
                        onMapViewClick = { latLng ->
                            Log.d("LatLng", "${latLng.latitude} - ${latLng.longitude}")
                            currentLat = latLng.latitude
                            currentLon = latLng.longitude
                            Log.d("LatLng", "$currentLat - $currentLon")
                            navController.navigate("map_screen")
                        }
                    )
                }
                composable("map_screen") { MapScreen(context = context, lat = currentLat, lon = currentLon) }
            }
        }
        is WeatherUIState.Loading -> LoadingScreen()
        is WeatherUIState.Empty -> Text("Không có dữ liệu hiển thị", modifier = Modifier.padding(16.dp))
        is WeatherUIState.Error -> ErrorScreen("Failed")
    }
}