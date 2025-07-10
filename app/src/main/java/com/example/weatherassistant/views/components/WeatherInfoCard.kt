package com.example.weatherassistant.views.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.weatherassistant.Model.WeatherFlashData
import java.util.Locale

@Composable
fun WeatherInfoCard(weatherData: WeatherFlashData?, onClose: () -> Unit) {
    if (weatherData == null) return

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Thông tin thời tiết",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                IconButton(onClick = { onClose() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color(0xFFFF6633)
                    )
                }
            }

            Divider(color = Color.White.copy(alpha = 0.2f))

            weatherData.latLng?.let { latLng ->
                // SỬA LẠI CÁCH GỌI HÀM FORMAT
                WeatherRowItem(label = "Vĩ độ", value = String.format(Locale.US, "%.4f", latLng.latitude))
                WeatherRowItem(label = "Kinh độ", value = String.format(Locale.US, "%.4f", latLng.longitude))
            } ?: run {
                WeatherRowItem(label = "Vị trí", value = "Không có dữ liệu")
            }
            WeatherRowItem(label = "Nhiệt độ", value = "${weatherData.temperature_2m}°C")
            WeatherRowItem(label = "Khả năng mưa trong 2h tới", value = "${weatherData.rain} mm")
        }
    }
}

@Composable
fun WeatherRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}