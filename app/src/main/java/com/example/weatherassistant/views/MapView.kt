package com.example.weatherassistant.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngQuad
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.RasterLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.ImageSource
import com.mapbox.mapboxsdk.style.sources.RasterSource
import com.mapbox.mapboxsdk.style.sources.TileSet
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sinh
import kotlin.math.tan
import kotlin.math.*
import com.example.weatherassistant.R
import com.example.weatherassistant.viewmodel.WeatherFlashViewModel
import com.example.weatherassistant.views.components.WeatherInfoCard
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import kotlin.apply
import kotlin.collections.forEachIndexed
import kotlin.collections.joinToString
import kotlin.collections.maxOf
import kotlin.collections.minOf
import kotlin.io.bufferedReader
import kotlin.io.readText
import kotlin.io.use
import kotlin.let
import kotlin.ranges.until

@Composable
fun MapScreen(
    context: Context,
    lat: Double,
    lon: Double,
    viewModel: WeatherFlashViewModel = viewModel()
) {
    // File Json at app/assets/osm.style.json
    val json = context.assets.open("osm_style.json").bufferedReader().use { it.readText() }
    // Update type of weather layer:
    var layerType by remember { mutableStateOf("none") }
    // Labels each type:
    val tapLabels = listOf("Cơ Bản", "Mức Gió", "Lượng Mưa", "Nhiệt độ")
    // update Which style is current:
    var currentStyle = remember { mutableStateOf<Style?>(null) }
    // update map by mapBoxMap:
    val mapBoxMap = remember { mutableStateOf<MapboxMap?>(null) }
    // Marker:
    val markerBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.ic_marker)
            .copy(Bitmap.Config.ARGB_8888, true)
    }


    val zoom = 3
    val tilesWide = 3
    val tileHigh = 3
    val centerLat = lat
    val centerLon = lon

    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxWidth().weight(0.9f)
        ) {
            AndroidView(
                factory = {
                    MapView(context).apply {
                        onCreate(null)
                        getMapAsync { map ->
                            mapBoxMap.value = map
                            // Enable all gesture on Screen (like: zoom in, zoom out, moving ...)
                            map.uiSettings.setAllGesturesEnabled(true)

                            // Set camera at first location:
                            map.setCameraPosition(
                                CameraPosition.Builder()
                                    .target(LatLng(centerLat, centerLon)) // ví dụ: Hà Nội
                                    .zoom(10.0)
                                    .build()
                            )

                            // Set map style from file osm_style.json:
                            map.setStyle(Style.Builder().fromJson(json)) { style ->
                                currentStyle.value = style

                                style.removeImage("marker-icon-id") // ← Xoá ảnh cũ nếu đã có
                                style.addImage("marker-icon-id", markerBitmap)

                                val feature =
                                    Feature.fromGeometry(Point.fromLngLat(lon, lat)).apply {
                                        addStringProperty("id", "marker1")
                                    }
                                val geoJsonSource =
                                    GeoJsonSource("marker-source-id", Point.fromLngLat(lon, lat))
                                style.addSource(geoJsonSource)

                                style.addLayer(
                                    SymbolLayer(
                                        "marker-layer-id",
                                        "marker-source-id"
                                    ).withProperties(
                                        iconImage("marker-icon-id"),
                                        iconSize(0.5f),
                                        iconAllowOverlap(true),
                                        iconIgnorePlacement(true)
                                    )
                                )

                                mapBoxMap.value?.addOnMapClickListener { point ->
                                    val screenPoint =
                                        mapBoxMap.value!!.projection.toScreenLocation(point)
                                    val features = mapBoxMap.value!!.queryRenderedFeatures(
                                        screenPoint,
                                        "marker-layer-id"
                                    )

                                    // Di chuyển marker đến vị trí click
                                    val source =
                                        style.getSource("marker-source-id") as? GeoJsonSource
                                    source?.setGeoJson(
                                        Feature.fromGeometry(
                                            Point.fromLngLat(
                                                point.longitude,
                                                point.latitude
                                            )
                                        )
                                    )

                                    // Gọi API lấy dữ liệu tại vị trí đó
                                    viewModel.fetchData(point.latitude, point.longitude)

                                    true // Consume sự kiện click
                                }

                                Log.d("WeatherAPI", "Gọi API tại: lat=$lat, lon=$lon")

                                // Add tiles Layer
                                if (layerType != "none") {
                                    getWeatherLayerFromCache(
                                        context,
                                        centerLat,
                                        centerLon,
                                        zoom,
                                        tilesWide,
                                        tileHigh,
                                        style,
                                        layerType
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            val weatherData by viewModel.weatherFlashData.collectAsState()
            weatherData?.let {
                WeatherInfoCard(
                    weatherData = it,
                    onClose = {
                        viewModel.clearWeatherData()
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize().weight(0.1f).padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            var selectedButton by remember { mutableStateOf(0) }

            tapLabels.forEachIndexed { index, label ->
                IconButton(
                    onClick = {
                        selectedButton = index
                        layerType = when (label) {
                            "Nhiệt độ" -> "temp_new"
                            "Mức Gió" -> "wind_new"
                            "Lượng Mưa" -> "precipitation_new"
                            else -> "none"
                        }
                        currentStyle.value?.let { style ->
                            runOnUiThread {
                                removeWeatherLayer(style)
                            }
                            if (layerType != "none") {
                                // Set Fixed zoom level:
                                mapBoxMap.value?.let { map ->
                                    map.setMinZoomPreference(3.0)
                                    map.setMaxZoomPreference(3.0)
                                    map.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(
                                                centerLat,
                                                centerLon
                                            ), 3.0
                                        )
                                    )
                                }
                                // Get Weather Layer
                                getWeatherLayerFromCache(
                                    context,
                                    centerLat,
                                    centerLon,
                                    zoom,
                                    tilesWide,
                                    tileHigh,
                                    style,
                                    layerType
                                )
                            } else {    // If none weather layer was chosen, unLock Map zoomability:
                                mapBoxMap.value?.let { map ->
                                    map.setMinZoomPreference(2.0)
                                    map.setMaxZoomPreference(20.0)
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(0.25f)
                        .background(color = Color.Transparent, shape = RectangleShape)
                        .clip(shape = RectangleShape)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            color = Color(if (selectedButton == index) 0xFF55DDFF else 0x88555555),
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedButton == index) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF55DDFF),
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Get tiles online:
fun addWeatherLayer(style: Style, type: String) {
    // Dam bao xoa layer cu truoc khi them moi:
    style.getLayer("weather-layer")?.let { style.removeLayer("weather-layer") }
    style.getSource("weather-source")?.let { style.removeSource("weather-source") }

    val tileSet = TileSet("2.1.0", "https://tile.openweathermap.org/map/${type}/{z}/{x}/{y}.png?appid=9148cead4a065cefee24e1bb76090e88")
    val rasterSource = RasterSource("weather-source", tileSet, 256)
    val rasterLayer = RasterLayer("weather-layer", "weather-source").apply {
        setProperties(PropertyFactory.rasterOpacity(0.6f))
    }
    style.addSource(rasterSource)
    style.addLayer(rasterLayer)
}

// Get tiles from cache:
fun getWeatherLayerFromCache(context: Context, lat: Double, lon: Double, zoomLevel: Int, tilesWide: Int, tilesHigh: Int, style: Style, layerType: String) {
    val tilesToDownload = getTilesToDownload(lat, lon, zoomLevel, tilesWide, tilesHigh)
    val filename = getCurrentHourKey(layerType)
    val cacheFile = File(context.cacheDir, filename)
    // If cache file exists, we just need to show the tiles according to the quad
    if (cacheFile.exists()) {
        // Determine Quad:
            // Determine 4 corners from tile-coordinate to LatLong coordinate:
        val minX = tilesToDownload.minOf { it.first }.toDouble()
        val maxX = tilesToDownload.maxOf { it.first }.toDouble()
        val minY = tilesToDownload.minOf { it.second }.toDouble()
        val maxY = tilesToDownload.maxOf { it.second }.toDouble()
            // Converting:
        val topLeft = tileXYToLatLng(minX, minY, zoomLevel)
        val topRight = tileXYToLatLng(maxX + 1, minY, zoomLevel)
        val bottomRight = tileXYToLatLng(maxX + 1, maxY + 1, zoomLevel)
        val bottomLeft = tileXYToLatLng(minX, maxY + 1, zoomLevel)
        // Đặt Log ở đây: Quan trọng nhất! Xem 4 góc của LatLngQuad
        Log.d("LatLngQuadDebug", "--- Final LatLngQuad for Overlay From getWeatherLayerFromCache ---")
        Log.d("LatLngQuadDebug", "TopLeft: Lat=${topLeft.latitude}, Lon=${topLeft.longitude}")
        Log.d("LatLngQuadDebug", "TopRight: Lat=${topRight.latitude}, Lon=${topRight.longitude}")
        Log.d("LatLngQuadDebug", "BottomRight: Lat=${bottomRight.latitude}, Lon=${bottomRight.longitude}")
        Log.d("LatLngQuadDebug", "BottomLeft: Lat=${bottomLeft.latitude}, Lon=${bottomLeft.longitude}")
        Log.d("LatLngQuadDebug", "-----------------------------------")

        // create Quad:
        val quad = LatLngQuad(topLeft, topRight, bottomRight, bottomLeft)
        Log.d("WeatherCache", "✅ Dùng ảnh đã cache: ${cacheFile.name}")
        runOnUiThread {
            showWeatherImageOverlay(style, cacheFile, quad)
        }
    } else {
        Log.d("WeatherCache", "⏬ Đang tải tile từ API...")
        downloadAndCacheWeatherTile(context, layerType, tilesToDownload, zoomLevel, cacheFile) { file, quad ->
            Log.d("WeatherCache", "💾 Đã lưu tile xuống cache: ${cacheFile.name}")
            runOnUiThread {
                showWeatherImageOverlay(style, file, quad)
            }
        }
    }
}

fun removeWeatherLayer(style: Style) {
    style.getLayer("weather-layer")?.let { style.removeLayer("weather-layer") }
    style.getSource("weather-source")?.let { style.removeSource("weather-source") }
}

// Create key for filename of cache:
fun getCurrentHourKey(layerType: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH", Locale.getDefault())
    val currentHourKey = dateFormat.format(Date())
    return "${currentHourKey}_$layerType.png"
}

// get Exactly Which tiles will be download or show: (List of Tiles referenced by (tileX, tileY)):
fun getTilesToDownload(
    lat: Double,
    lon: Double,
    zoomLevel: Int,
    tilesWide: Int,
    tilesHigh: Int
): List<Pair<Int, Int>> {
    // Determine X and Y Tile-coordinate of the Center location (lat, lon)
    val (centralTileX, centralTileY) = latLngToTileXY(lat = lat, lon = lon, zoom = zoomLevel)

    // Đặt Log ở đây: Kiểm tra tile trung tâm và điểm bắt đầu của lưới
    Log.d("TileDebug", "Center LatLon: (${lat}, ${lon})")
    Log.d("TileDebug", "Calculated Central Tile (Int): (${centralTileX}, ${centralTileY}) for Zoom ${zoomLevel}")

    // Determine X,Y Tile-coordinate of the top-left Tile from the centralTile:
    val startTileX = centralTileX - (tilesWide / 2)
    val startTileY = centralTileY - (tilesHigh / 2)

    // Define List of Tiles:
    val tiles = mutableListOf<Pair<Int, Int>>()
    // Conduct Adding new tile determinated by the topleft Tile-coordinate:
    for (x in startTileX until (startTileX + tilesWide)) {
        for (y in startTileY until (startTileY + tilesHigh)) {
            tiles.add(Pair(x, y))
        }
    }
    // Đặt Log ở đây: Hiển thị toàn bộ danh sách các tile sẽ tải
    Log.d("TileDebug", "Tiles to Download (X,Y) at Zoom ${zoomLevel}: ${tiles.joinToString()}")
    return tiles
}

// Convert Lat long to tile X Y
fun latLngToTileXY1(lat: Double, lon: Double, zoom: Int): Pair<Int, Int> {
    val n = 2.0.pow(zoom)
    val x = ((lon + 180) / 360) * n
    val latRadius = lat * PI /180
    val y = ((1.0 - ln(tan(latRadius) + 1 / cos(latRadius))) / PI) / 2.0 * n
    // Đặt Log ở đây: Xem tọa độ tile (có phần thập phân) và (số nguyên)
    Log.d("TileDebug", "LatLng (${lat}, ${lon}) at Zoom ${zoom} -> Raw Tile: (${x}, ${y})")
    Log.d("TileDebug", "LatLng (${lat}, ${lon}) at Zoom ${zoom} -> Int Tile: (${x.toInt()}, ${y.toInt()})")
    return Pair(x.toInt(), y.toInt())
}
// Show Weather Image Overlay:
fun tileXYToLatLng1(x: Double, y: Double, zoom: Int): LatLng {
    val n = 2.0.pow(zoom)
    val lon = x / n * 360 - 180
    val latRadius = atan(sinh(PI * (1 - 2 * y / n)))
    val lat = latRadius * 180/ PI
    // Đặt Log ở đây: Xem tọa độ LatLng của góc tile
    Log.d("TileDebug", "Tile (${x}, ${y}) at Zoom ${zoom} -> LatLng (TopLeft Corner): (${lat}, ${lon})")
    return LatLng(lat, lon)
}

fun showWeatherImageOverlay(style: Style, file: File, quad: LatLngQuad) {
    // Dam bao xoa cac source va layer cu:
    style.getLayer("weather-layer")?.let { style.removeLayer("weather-layer")}
    style.getSource("weather-overlay")?.let { style.removeSource("weather-overlay") }

    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
    // Log Error if cannot decode Bitmap
    if (bitmap == null)
        Log.e("WeatherOverLay", "❌ Không thể Decode Bitmap từ file: ${file.absolutePath}")

    val imageSource = ImageSource("weather-overlay", quad, bitmap)
    val imageLayer = RasterLayer("weather-layer", "weather-overlay")
        .withProperties(PropertyFactory.rasterOpacity(1.5f))
    style.addSource(imageSource)
    style.addLayer(imageLayer)
}

// Download and Cache Weather Tile
fun downloadAndCacheWeatherTile(
    context: Context,
    layerType: String,
    tilesToDownload: List<Pair<Int, Int>>,
    zoomLevel: Int,
    outputFile: File,
    onComplete: (File, LatLngQuad) -> Unit
) {
    val tileSize = 256
    val minX = tilesToDownload.minOf { it.first }
    val maxX = tilesToDownload.maxOf { it.first }
    val minY = tilesToDownload.minOf { it.second }
    val maxY = tilesToDownload.maxOf { it.second }
    val widthInTiles = maxX - minX + 1
    val heightInTiles = maxY - minY + 1
    val bitmap = Bitmap.createBitmap(widthInTiles * tileSize, heightInTiles * tileSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val client = OkHttpClient()

    var remaining = tilesToDownload.size

    for ((x, y) in tilesToDownload) {
        val url = "https://tile.openweathermap.org/map/$layerType/$zoomLevel/$x/$y.png?appid=9148cead4a065cefee24e1bb76090e88"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TileDownload", "❌ Lỗi tải tile: $url", e)
                remaining--
                if (remaining == 0) {
                    // Proccessing Whether request completed or failed:
                    // Convert 4 corners of the image of tiles into quad (4 coordinates of corresponding corners):
                    val topLeft = tileXYToLatLng(minX.toDouble(), minY.toDouble(), zoomLevel)
                    val topRight =
                        tileXYToLatLng((maxX + 1).toDouble(), minY.toDouble(), zoomLevel)
                    val bottomRight =
                        tileXYToLatLng((maxX + 1).toDouble(), (maxY + 1).toDouble(), zoomLevel)
                    val bottomLeft =
                        tileXYToLatLng(minX.toDouble(), (maxY + 1).toDouble(), zoomLevel)
                    // Đặt Log ở đây: Quan trọng nhất! Xem 4 góc của LatLngQuad
                    Log.d("LatLngQuadDebug", "--- Final LatLngQuad for Overlay From downloadAndCacheWeatherTile() ---")
                    Log.d("LatLngQuadDebug", "TopLeft: Lat=${topLeft.latitude}, Lon=${topLeft.longitude}")
                    Log.d("LatLngQuadDebug", "TopRight: Lat=${topRight.latitude}, Lon=${topRight.longitude}")
                    Log.d("LatLngQuadDebug", "BottomRight: Lat=${bottomRight.latitude}, Lon=${bottomRight.longitude}")
                    Log.d("LatLngQuadDebug", "BottomLeft: Lat=${bottomLeft.latitude}, Lon=${bottomLeft.longitude}")
                    Log.d("LatLngQuadDebug", "-----------------------------------")
                    val quad = LatLngQuad(topLeft, topRight, bottomRight, bottomLeft)
                    onComplete(outputFile, quad)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.let { body ->
                    val bytes = body.bytes()
                    val tileBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    if (tileBitmap != null) {
                        synchronized(bitmap) {
                            val tileXOffset = x - minX
                            val tileYOffset = y - minY
                            canvas.drawBitmap(
                                tileBitmap,
                                tileXOffset * tileSize.toFloat(),
                                tileYOffset * tileSize.toFloat(),
                                null
                            )
                        }
                    } else {
                        Log.e("TileDownload", "❌ Không thể decode ảnh tile: $url")
                    }

                }

                remaining--
                if (remaining == 0) {
                    // Save Image when all tiles have been loaded:
                    FileOutputStream(outputFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    //
                    val topLeft = tileXYToLatLng(minX.toDouble(), minY.toDouble(), zoomLevel)
                    val topRight = tileXYToLatLng((maxX + 1).toDouble(), minY.toDouble(), zoomLevel)
                    val bottomRight = tileXYToLatLng((maxX + 1).toDouble(), (maxY + 1).toDouble(), zoomLevel)
                    val bottomLeft = tileXYToLatLng(minX.toDouble(), (maxY + 1).toDouble(), zoomLevel)
                    val quad = LatLngQuad(topLeft, topRight, bottomRight, bottomLeft)
                    runOnUiThread {
                        onComplete(outputFile, quad)
                    }
                }
            }

        })
    }
}

// Forcing run on UI thread Function:
fun runOnUiThread(action: () -> Unit) {
    Handler(Looper.getMainLooper()).post(action)
}

// Cập nhật hàm latLngToTileXY
fun latLngToTileXY(lat: Double, lon: Double, zoom: Int): Pair<Int, Int> {
    val n = 2.0.pow(zoom.toDouble()) // n là 2^zoom, cần đảm bảo là Double
    val xTile = floor((lon + 180.0) / 360.0 * n).toInt()
    val latRad = Math.toRadians(lat) // Chuyển đổi độ sang radian cho sin/atanh
    val yTile = floor((1.0 - asinh(tan(latRad)) / PI) / 2.0 * n).toInt() // asinh là arcsinh
    return Pair(xTile, yTile)
}

// Cập nhật hàm tileXYToLatLng
fun tileXYToLatLng(x: Double, y: Double, zoom: Int): LatLng {
    val n = 2.0.pow(zoom.toDouble()) // n là 2^zoom, cần đảm bảo là Double
    val lon_deg = x / n * 360.0 - 180.0
    val lat_rad = atan(sinh(PI * (1 - 2 * y / n)))
    val lat_deg = Math.toDegrees(lat_rad) // Chuyển đổi radian sang độ
    return LatLng(lat_deg, lon_deg)
}

// Lưu ý: Thêm hàm asinh (arcsinh) nếu chưa có
// Java/Kotlin Math không có asinh trực tiếp, bạn có thể tự định nghĩa
fun asinh(x: Double): Double {
    return ln(x + sqrt(x * x + 1.0))
}