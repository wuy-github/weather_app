package com.example.weatherassistant.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherassistant.R
import java.time.LocalDate
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@Composable
fun MainInfo(condition: String, date: LocalDate, temp: Double, maxTemp: Double, minTemp: Double){
    val context = LocalContext.current

    val mainIconResId = parseResIdFromTitle(context = context, title = condition, prefix = "main_icon_", oldSeparatedChar = '-', newSeparatedChar = '_')

    Row(
        modifier = Modifier.fillMaxWidth()
            .fillMaxHeight(0.37f)
            .padding(horizontal = 10.dp)
            .padding(top = 15.dp)
    ){
        // Main Icon and condition:
        Column(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally // căn giữa icon + text
        ) {
            Icon(
                painter = painterResource(mainIconResId),
                contentDescription = "Main Icon",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 4.dp),
                tint = Color.Unspecified
            )

            val displayText = when(condition.lowercase()) {
                "partly-cloudy" -> "Ít mây☁\uFE0F"
                "cloudy" -> "Nhiều mây☁\uFE0F"
                "rain" -> " Có Mưa\uD83C\uDF27\uFE0F"
                "partly_cloudy_night" -> "Đêm có một ít mây  "
                "clear_day" -> "Trời trong quang đãng☀\uFE0F"
                "clear_night" -> "Trời tối quang đãng có sao ⭐"
                "partly_cloudy_day" -> "Trời nhiều mây☁\uFE0F "
                else -> condition.replace('-', ' ')
            }

            Text(
                text = if (condition.isEmpty()) "Đang tải..." else displayText ,
                fontSize = 25.sp,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.robotomono_bold)),
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.001f))
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }


        // Current main information:
        Column(
            modifier = Modifier.weight(1f).background(Color.White.copy(alpha = 0.001f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = WhatTheDay(date),
                fontSize = 26.sp,
                maxLines = 1,
                color = Color(0xDD000000),
                fontFamily = FontFamily(Font(R.font.robotomono_bold)))
            Text(
                text = "$temp°C",
                fontSize = 40.sp,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.robotomono_bold)),
                lineHeight = 81.sp,
                maxLines = 2,
                softWrap = true,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(0.9f) // chiếm 90% chiều ngang của column
                    .align(Alignment.CenterHorizontally),

            )

            Text(
                text = "Cao nhất: " + "${maxTemp}" + "°C",
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.robotomono_semibolditalic)),
                maxLines = 1,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            )
            Text(
                text = "Thấp nhất: " + "${minTemp}" + "°C",
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.robotomono_semibolditalic)),
                maxLines = 1,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            )
        }
    }

}

@Composable
fun DateContainer(date: LocalDate){
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = getWeekDate(date),
            fontSize = 25.sp,
            color = Color.Black,
            fontFamily = FontFamily(Font(R.font.robotomono_bold))
        )
        Text(
            text = getDayDetail(date),
            fontSize = 15.sp,
            fontFamily = FontFamily(Font(R.font.robotomono_medium))
        )
    }
}

