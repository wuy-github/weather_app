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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherassistant.UIState.ErrorScreen
import com.example.weatherassistant.UIState.LoadingScreen
import com.example.weatherassistant.UIState.WeatherUIState
import com.example.weatherassistant.data.remote.saveFCMTokenAndLocationToFirestore
import com.example.weatherassistant.data.repository.WeatherFlashDataRepository
import com.example.weatherassistant.viewmodel.WeatherDataViewModel
import com.example.weatherassistant.viewmodel.WeatherFlashViewModel
import com.example.weatherassistant.views.LocationHistoryScreen
import com.example.weatherassistant.views.MainScreen
import com.example.weatherassistant.views.MapScreen

@Composable
fun WeatherApp(
    viewModel: WeatherDataViewModel,
    notificationTitle: String?,
    notificationBody: String?,
    deviceToken: String?
) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val context = LocalContext.current
    var hasFetched by remember { mutableStateOf(false) }
    var hasHandledNotification by remember { mutableStateOf(false) }
    val repository = remember { WeatherFlashDataRepository(context) }
    val flashViewModel: WeatherFlashViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WeatherFlashViewModel(repository) as T
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasFetched) {
            viewModel.fetchCurrentLocationWeatherData(context = context)
            hasFetched = true
        }
    }

    // ✅ Điều hướng từ Notification nếu có title/body
    LaunchedEffect(notificationTitle, notificationBody) {
        if (!notificationTitle.isNullOrEmpty() && !notificationBody.isNullOrEmpty() && !hasHandledNotification) {
            navController.navigate("map_screen")
            hasHandledNotification = true
        }
    }

    LaunchedEffect(viewModel.wholeResponseData) {
        viewModel.wholeResponseData.value?.let { location ->
            flashViewModel.fetchData(location.latitude, location.longitude)
        }
    }

    val wholeData by viewModel.wholeResponseData.collectAsState()

    // 👇 1. Dùng remember và mutableStateOf để lưu trạng thái tọa độ
    var currentLat by remember(wholeData) { mutableStateOf(wholeData?.latitude ?: 21.0333) }
    var currentLon by remember(wholeData) { mutableStateOf(wholeData?.longitude ?: 105.8500) }

    // Save User's DeviceToken and Current Location on the Firebase store:
    deviceToken?.let { token ->
        saveFCMTokenAndLocationToFirestore(token, currentLat, currentLon)
    }
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
                        weatherDataViewModel = viewModel,
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