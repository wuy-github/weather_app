package com.example.weatherassistant.views.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherassistant.Model.WeatherData
import com.example.weatherassistant.R

@Composable
fun DetailInfo(data: WeatherData){
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .heightIn(min = 100.dp)
            .border(width = 3.dp, color = Color.White, shape = RoundedCornerShape(percent = 12))
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row for 3 cards
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoCard(title = "humidity", data = data.humidity.toString() + " %", context = context, modifier = Modifier.weight(2f))
            InfoCard(title = "wind", data = data.windSpeed.toString() + " km/h", context = context, modifier = Modifier.weight(2f))
            InfoCard(title = "pressure", data = data.pressure.toString() + " hPa", context = context, modifier = Modifier.weight(2f))
        }
        // Second Row:
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoCard(title = "sunrise", data = data.sunrise, context = context, modifier = Modifier.weight(2f))
            InfoCard(title = "sunset", data = data.sunset, context = context, modifier = Modifier.weight(2f))
            InfoCard(title = "uv", data = data.uvIndex.toString() + evaluateUVLevel(data.uvIndex), context = context, modifier = Modifier.weight(2f))
        }
    }
}

@Composable
fun InfoCard(title: String, data: String, context: Context? = null, iconResId: Int? = R.drawable.icon_error, modifier: Modifier = Modifier){
    val iconId = if(context != null) parseResIdFromTitle(context = context, title = title, prefix = "icon_", oldSeparatedChar = '-', newSeparatedChar = '_')
                        else iconResId
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .padding(top = 5.dp),
        shape = RoundedCornerShape(percent = 20),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        border = BorderStroke(2.dp, Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                painter = painterResource(iconId ?: R.drawable.icon_error),
                contentDescription = title,
                modifier = Modifier.fillMaxHeight(0.4f)
            )
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
            )
            Text(
                text = data,
                fontSize = 15.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}