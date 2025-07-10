package com.example.weatherassistant

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

    LaunchedEffect(Unit) {
        if (!hasFetched) {
            viewModel.getCurrentLocation(context = context) { location ->
                if (location == null) {
                    viewModel.setError("Không lấy được vị trí thiết bị")
                    Log.e("CurrentLocation", "❌ Không lấy được vị trí hiện tại")
                }
            }
            hasFetched = true
        }
    }

    val navController = rememberNavController()
    val wholeData by viewModel.wholeResponseData.collectAsState()

    // 👇 1. Dùng remember và mutableStateOf để lưu trạng thái tọa độ
    var currentLat by remember(wholeData) { mutableStateOf(wholeData?.latitude ?: 21.0333) }
    var currentLon by remember(wholeData) { mutableStateOf(wholeData?.longitude ?: 105.8500) }

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
                        // Giả sử bạn có một nút "Xem bản đồ" trong LocationHistoryScreen
                        // Khi nhấn nút đó, nó sẽ cập nhật tọa độ và điều hướng
                        onMapViewClick = { latLng ->
                            currentLat = latLng.latitude
                            currentLon = latLng.longitude
                            navController.navigate("map_screen")
                        }
                    )
                }
                composable("map_screen") {
                    MapScreen(
                        context = context,
                        lat = currentLat,
                        lon = currentLon,

                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
        is WeatherUIState.Loading -> LoadingScreen()
        is WeatherUIState.Empty -> Column(Modifier.fillMaxSize().background(Color.White)) {}
        is WeatherUIState.Error -> ErrorScreen("Failed")
    }
}