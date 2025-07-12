package com.example.weatherassistant.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.mapbox.mapboxsdk.geometry.LatLng
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.*

class MapRepository(private val context: Context) {

    private val client = OkHttpClient()

    fun fetchAndCacheTiles(
        layerType: String,
        tilesToDownload: List<Pair<Int, Int>>,
        zoomLevel: Int,
        onComplete: (File, Pair<LatLng, LatLng>) -> Unit
    ) {
        val filename = getCurrentHourKey(layerType)
        val cacheFile = File(context.cacheDir, filename)
        val quadCorners = getQuadCorners(tilesToDownload, zoomLevel)

        if (cacheFile.exists()) {
            Log.d("MapRepository", "✅ Dùng ảnh đã cache: ${cacheFile.name}")
            onComplete(cacheFile, quadCorners)
            return
        }

        Log.d("MapRepository", "⏬ Đang tải tile từ API...")
        downloadAndStitchTiles(layerType, tilesToDownload, zoomLevel, cacheFile) { file ->
            onComplete(file, quadCorners)
        }
    }

    // 👇 SỬA LẠI HÀM NÀY ĐỂ XỬ LÝ ĐỒNG BỘ TỐT HƠN
    private fun downloadAndStitchTiles(
        layerType: String, tilesToDownload: List<Pair<Int, Int>>, zoomLevel: Int,
        outputFile: File, onComplete: (File) -> Unit
    ) {
        val tileSize = 256
        val minX = tilesToDownload.minOfOrNull { it.first } ?: 0
        val maxX = tilesToDownload.maxOfOrNull { it.first } ?: 0
        val minY = tilesToDownload.minOfOrNull { it.second } ?: 0
        val maxY = tilesToDownload.maxOfOrNull { it.second } ?: 0
        val widthInTiles = maxX - minX + 1
        val heightInTiles = maxY - minY + 1

        if (widthInTiles <= 0 || heightInTiles <= 0) return

        val finalBitmap = Bitmap.createBitmap(widthInTiles * tileSize, heightInTiles * tileSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalBitmap)

        // Dùng AtomicInteger để đảm bảo an toàn khi nhiều thread cùng cập nhật
        val remaining = AtomicInteger(tilesToDownload.size)

        for ((x, y) in tilesToDownload) {
            val url = "https://tile.openweathermap.org/map/$layerType/$zoomLevel/$x/$y.png?appid=9148cead4a065cefee24e1bb76090e88"
            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : Callback {
                private fun onTileFinished() {
                    // Giảm biến đếm và kiểm tra xem đã xong hết chưa
                    if (remaining.decrementAndGet() == 0) {
                        FileOutputStream(outputFile).use { out ->
                            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        Log.d("MapRepository", "💾 Đã lưu tile xuống cache: ${outputFile.name}")
                        runOnUiThread {
                            onComplete(outputFile)
                        }
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.e("MapRepository", "❌ Lỗi tải tile: $url", e)
                    onTileFinished()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.use { body ->
                        val tileBitmap = BitmapFactory.decodeStream(body.byteStream())
                        if (tileBitmap != null) {
                            synchronized(finalBitmap) {
                                val tileXOffset = x - minX
                                val tileYOffset = y - minY
                                canvas.drawBitmap(tileBitmap, (tileXOffset * tileSize).toFloat(), (tileYOffset * tileSize).toFloat(), null)
                            }
                        }
                    }
                    onTileFinished()
                }
            })
        }
    }

    private fun getQuadCorners(tilesToDownload: List<Pair<Int, Int>>, zoomLevel: Int): Pair<LatLng, LatLng> {
        val minX = tilesToDownload.minOf { it.first }.toDouble()
        val maxX = tilesToDownload.maxOf { it.first }.toDouble()
        val minY = tilesToDownload.minOf { it.second }.toDouble()
        val maxY = tilesToDownload.maxOf { it.second }.toDouble()
        val topLeft = tileXYToLatLng(minX, minY, zoomLevel)
        val bottomRight = tileXYToLatLng(maxX + 1, maxY + 1, zoomLevel)
        return Pair(topLeft, bottomRight)
    }

    private fun getCurrentHourKey(layerType: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH", Locale.US)
        return "${dateFormat.format(Date())}_${layerType}.png"
    }

    fun getTilesToDownload(lat: Double, lon: Double, zoom: Int, tilesWide: Int, tilesHigh: Int): List<Pair<Int, Int>> {
        val (centralTileX, centralTileY) = latLngToTileXY(lat, lon, zoom)
        val startTileX = centralTileX - (tilesWide / 2)
        val startTileY = centralTileY - (tilesHigh / 2)
        val tiles = mutableListOf<Pair<Int, Int>>()
        for (x in startTileX until (startTileX + tilesWide)) {
            for (y in startTileY until (startTileY + tilesHigh)) {
                tiles.add(Pair(x, y))
            }
        }
        return tiles
    }

    private fun runOnUiThread(action: () -> Unit) {
        Handler(Looper.getMainLooper()).post(action)
    }

    private fun latLngToTileXY(lat: Double, lon: Double, zoom: Int): Pair<Int, Int> {
        val n = 2.0.pow(zoom.toDouble())
        val xTile = floor((lon + 180.0) / 360.0 * n).toInt()
        val latRad = Math.toRadians(lat)
        val yTile = floor((1.0 - asinh(tan(latRad)) / PI) / 2.0 * n).toInt()
        return Pair(xTile, yTile)
    }

    private fun tileXYToLatLng(x: Double, y: Double, zoom: Int): LatLng {
        val n = 2.0.pow(zoom.toDouble())
        val lon_deg = x / n * 360.0 - 180.0
        val lat_rad = atan(sinh(PI * (1 - 2 * y / n)))
        val lat_deg = Math.toDegrees(lat_rad)
        return LatLng(lat_deg, lon_deg)
    }

    private fun asinh(x: Double): Double = ln(x + sqrt(x * x + 1.0))
}