package com.example.weatherassistant.views.components

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationSearching
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.weatherassistant.viewmodel.WeatherDataViewModel

@Composable
fun CurrentLocationButton(context: Context, viewModel: WeatherDataViewModel, enableLabel: Boolean = true, onCLick: (Location) -> Unit) {
    OutlinedButton(
        onClick = {
            var currentLocation: Location
            viewModel.getCurrentLocation(context) { location ->
                if (location == null) {
                    viewModel.setError("Không lấy được vị trí thiết bị")
                    Log.e("CurrentLocation", "❌ Không lấy được vị trí hiện tại")
                } else {
                    currentLocation = location
                    onCLick(currentLocation)
                }
            }
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationSearching,
                contentDescription = null,
                tint = Color(0xFFFF8833),
                modifier = Modifier.size(20.dp)
            )
            if (enableLabel) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get current\nlocation")
            }
        }
    }
}
