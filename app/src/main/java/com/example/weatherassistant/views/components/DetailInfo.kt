package com.example.weatherassistant.views.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherassistant.Model.WeatherData
import com.example.weatherassistant.R
import com.example.weatherassistant.data.model.WeatherDay
import com.example.weatherassistant.data.model.WeatherHour

@Composable
fun InfoPager(context: Context, data: WeatherData, hourLyDatas: List<WeatherHour>) {
    val pagerState = rememberPagerState(pageCount = {2})
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1.5f)
            .border(width = 3.dp, color = Color.White, shape = RoundedCornerShape(percent = 12))
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when(page) {
                0 -> DetailInfo(data)
                1 -> HourlyForecast(context, hourLyDatas)
            }
        }
    }
}

@Composable
fun DetailInfo(data: WeatherData){
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
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

@Composable
fun HourlyForecast(context: Context, hourlyDatas: List<WeatherHour>) {
    val chunkedList = hourlyDatas.chunked(3) // Tạo các nhóm 3 giờ
    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        chunkedList.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowItems.forEach { data ->
                    val iconResId = parseResIdFromTitle(context, title = data.icon, prefix = "main_icon_", oldSeparatedChar = '-', newSeparatedChar = '_')
                    val safeId = if (iconResId > 0 && iconResId != null) iconResId else R.drawable.icon_error
                    HourlyDataCard(data.datetime, "${data.temp}°C", safeId)
                }

                // Nếu dòng này chưa đủ 3 ô (cuối cùng), thêm ô trống
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun HourlyDataCard(hour: String, temp: String = "33°C", imageResId: Int) {
    Column(
        modifier = Modifier.padding(10.dp).background(brush = Brush.verticalGradient(
            colors = listOf(Color(0x22777777), Color(0x65FFFFFF)),
            startY = 0.0f,
            endY = Float.POSITIVE_INFINITY
        ), shape = RoundedCornerShape(5.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            painter = painterResource(imageResId),
            contentDescription = null,
            modifier = Modifier.size(50.dp),
            tint = Color.Unspecified
        )
        Text(text = temp)
        Text(hour)
    }
}