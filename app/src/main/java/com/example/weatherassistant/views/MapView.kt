package com.example.weatherassistant.views

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherassistant.R
import com.example.weatherassistant.viewmodel.MapViewModel
import com.example.weatherassistant.viewmodel.WeatherFlashViewModel
import com.example.weatherassistant.views.components.WeatherInfoCard
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    context: Context,
    lat: Double,
    lon: Double,
    flashViewModel: WeatherFlashViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val mapViewModel: MapViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(application)
    )

    val json = context.assets.open("osm_style.json").bufferedReader().use { it.readText() }
    val markerBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.ic_marker)
            .copy(Bitmap.Config.ARGB_8888, true)
    }
    var mapboxMap by remember { mutableStateOf<com.mapbox.mapboxsdk.maps.MapboxMap?>(null) }

    // Theo dõi sự thay đổi của layerType để ra lệnh cho ViewModel
    LaunchedEffect(mapViewModel.layerType.value) {
        mapViewModel.updateWeatherLayer(lat, lon, 3)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bản đồ thời tiết") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            Box(Modifier.weight(1f)) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        MapView(context).apply {
                            onCreate(null)
                            getMapAsync { map ->
                                mapboxMap = map
                                map.uiSettings.setAllGesturesEnabled(true)
                                map.setCameraPosition(
                                    CameraPosition.Builder().target(LatLng(lat, lon)).zoom(10.0).build()
                                )
                                map.setStyle(Style.Builder().fromJson(json)) { style ->
                                    mapViewModel.mapStyle.value = style // Lưu style vào ViewModel
                                    style.addImage("marker-icon-id", markerBitmap)
                                    val source = GeoJsonSource("marker-source-id", Point.fromLngLat(lon, lat))
                                    style.addSource(source)
                                    style.addLayer(
                                        SymbolLayer("marker-layer-id", "marker-source-id").withProperties(
                                            PropertyFactory.iconImage("marker-icon-id"),
                                            PropertyFactory.iconSize(0.5f)
                                        )
                                    )
                                    map.addOnMapClickListener { point ->
                                        (style.getSource("marker-source-id") as? GeoJsonSource)
                                            ?.setGeoJson(Feature.fromGeometry(Point.fromLngLat(point.longitude, point.latitude)))
                                        flashViewModel.fetchData(point.latitude, point.longitude)
                                        true
                                    }
                                }
                            }
                        }
                    }
                )
                val weatherData by flashViewModel.weatherFlashData.collectAsState()
                weatherData?.let {
                    WeatherInfoCard(weatherData = it, onClose = { flashViewModel.clearWeatherData() })
                }
            }
            BottomActionBar(selectedLayer = mapViewModel.layerType.value) { newLayer ->
                mapViewModel.layerType.value = newLayer

                if (newLayer != "none") {
                    mapboxMap?.let { map ->
                        map.setMinZoomPreference(3.0)
                        map.setMaxZoomPreference(3.0)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 3.0))
                    }
                } else {
                    mapboxMap?.let { map ->
                        map.setMinZoomPreference(2.0)
                        map.setMaxZoomPreference(20.0)
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomActionBar(selectedLayer: String, onLayerSelected: (String) -> Unit) {
    val tapLabels = mapOf(
        "none" to "Cơ Bản", "temp_new" to "Nhiệt độ",
        "precipitation_new" to "Lượng Mưa", "wind_new" to "Mức Gió"
    )
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tapLabels.forEach { (layerKey, label) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).clickable { onLayerSelected(layerKey) }.padding(vertical = 4.dp)
            ) {
                Text(text = label, color = if (selectedLayer == layerKey) Color.Cyan else Color.White, fontSize = 14.sp)
                if (selectedLayer == layerKey) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(modifier = Modifier.height(2.dp).width(24.dp).background(Color.Cyan))
                }
            }
        }
    }
}