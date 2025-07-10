package com.example.weatherassistant.views

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherassistant.R
import com.example.weatherassistant.viewmodel.WeatherDataViewModel
import com.mapbox.mapboxsdk.geometry.LatLng

private val backgroundList = listOf(
    R.drawable.bg_clear_day, R.drawable.bg_clear_night, R.drawable.bg_cloudy,
    R.drawable.bg_rain, R.drawable.bg_snow, R.drawable.bg_sunny,
    R.drawable.bg_partly_cloudy_day, R.drawable.bg_partly_cloudy_night
)

private val cardColorList = listOf(
    Color(0xFFB2EBF2), Color(0xFFC8E6C9), Color(0xFFFFF9C4),
    Color(0xFFF8BBD0), Color(0xFFD1C4E9), Color(0xFFFFE0B2)
)

private enum class OptionsViewState { GRID, HISTORY, NEARBY, MAP }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationHistoryScreen(
    viewModel: WeatherDataViewModel,
    onNavigateBack: () -> Unit,
    onHistoryItemClick: (String) -> Unit, // Callback cho Lịch sử
    onNearbyPlaceClick: (String) -> Unit,  // 👇 THÊM CALLBACK MỚI cho Địa điểm gần đây
    onMapViewClick: (LatLng) -> Unit
) {
    var currentView by remember { mutableStateOf(OptionsViewState.GRID) }
    val randomBackground = remember { backgroundList.random() }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = randomBackground),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (currentView) {
                                OptionsViewState.GRID -> "Tùy chọn"
                                OptionsViewState.HISTORY -> "Lịch sử tìm kiếm"
                                OptionsViewState.NEARBY -> "Địa điểm gần đây"
                                OptionsViewState.MAP -> "Bản đồ thời tiết"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (currentView != OptionsViewState.GRID) {
                                currentView = OptionsViewState.GRID
                            } else {
                                onNavigateBack()
                            }
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") }
                    },
                    actions = {
                        if (currentView == OptionsViewState.HISTORY) {
                            IconButton(onClick = { viewModel.clearSearchHistory() }) {
                                Icon(Icons.Default.Delete, "Xóa lịch sử")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.3f),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when (currentView) {
                    OptionsViewState.GRID -> OptionGrid(
                        onShowHistoryClick = { currentView = OptionsViewState.HISTORY },
                        onShowNearbyClick = { currentView = OptionsViewState.NEARBY },
                        onShowMapViewClick = { currentView = OptionsViewState.MAP}
                    )
                    OptionsViewState.HISTORY -> HistoryList(viewModel, onHistoryItemClick)
                    // 👇 Truyền callback mới vào
                    OptionsViewState.NEARBY -> NearbyPlacesList(viewModel, onLocationClick = onNearbyPlaceClick)
                    // Map Option:
                    OptionsViewState.MAP -> {
                        val lat = viewModel.wholeResponseData.value?.latitude ?: 0.0
                        val lon = viewModel.wholeResponseData.value?. longitude ?: 0.0
                        Log.d("LatLng", "Lat: $lat - Lon: $lon - in LocationHistoryScreen")
                        onMapViewClick(LatLng(lat, lon))
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionGrid(onShowHistoryClick: () -> Unit, onShowNearbyClick: () -> Unit, onShowMapViewClick: () -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(   16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { OptionCard("Lịch sử", Icons.Default.History, onShowHistoryClick) }
        item { OptionCard("Gần đây", Icons.Default.Place, onShowNearbyClick) }
        item { OptionCard("Bản đồ thời tiết", Icons.Filled.Map, onShowMapViewClick) }
    }
}

@Composable
private fun OptionCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    val randomColor = remember { cardColorList.random() }
    Card(
        modifier = Modifier.aspectRatio(1f).clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = randomColor.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, title, modifier = Modifier.size(48.dp), tint = Color.Black.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 18.sp, color = Color.Black.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun HistoryList(viewModel: WeatherDataViewModel, onHistoryItemClick: (String) -> Unit) {
    val history by viewModel.searchHistory.collectAsState()
    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(16.dp), Alignment.Center) {
            Text("Chưa có lịch sử tìm kiếm.", color = Color.White, fontSize = 18.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 8.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(history.toList().reversed()) { location ->
                Text(location, fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().clickable { onHistoryItemClick(location) }.padding(vertical = 12.dp)
                )
                HorizontalDivider(color = Color.Gray)
            }
        }
    }
}

@Composable
fun NearbyPlacesList(viewModel: WeatherDataViewModel, onLocationClick: (String) -> Unit) {
    val nearbyPlaces by viewModel.nearbyPlaces.collectAsState()
    if (nearbyPlaces.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(16.dp), Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 8.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(nearbyPlaces) { place ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onLocationClick(place.title) }.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Địa điểm",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = place.title, fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                if (nearbyPlaces.last() != place) {
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}