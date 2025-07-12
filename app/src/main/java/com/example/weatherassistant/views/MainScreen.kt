package com.example.weatherassistant.views

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherassistant.viewmodel.WeatherDataViewModel
import com.example.weatherassistant.views.components.DateContainer
import com.example.weatherassistant.views.components.DaySwitchingButton
import com.example.weatherassistant.views.components.InfoPager
import com.example.weatherassistant.views.components.ErrorData
import com.example.weatherassistant.views.components.LocationButton
import com.example.weatherassistant.views.components.MainInfo
import com.example.weatherassistant.views.components.NotificationBanner
import com.example.weatherassistant.views.components.SearchBar
import com.example.weatherassistant.views.components.parseResIdFromTitle
import java.time.LocalTime
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.weatherassistant.views.components.CurrentLocationButton


@Composable
fun MainScreen(
    viewModel: WeatherDataViewModel = viewModel(),
    context: Context,
    onLocationClick: () -> Unit
){
    val wholeData by viewModel.wholeResponseData.collectAsState()
    var dayIndex by remember { mutableStateOf(viewModel.getTodayIndex() ?: 0) }

    val data  by  remember (dayIndex, wholeData) {
        derivedStateOf {
            if (viewModel.listDaysData.isNotEmpty()){
                viewModel.getWeatherDataByHour(dayIndex, LocalTime.now().hour)
            } else ErrorData
        }
    }
    val hourDatas by remember (dayIndex, wholeData) {
        derivedStateOf {
            if (viewModel.listDaysData.isNotEmpty()) {
                wholeData?.days?.getOrNull(dayIndex)?.hours ?: emptyList()
            } else {
                emptyList()
            }
        }
    }
    val notificationMessage = viewModel.notification.collectAsState()
    var mainBackground = remember(data.condition) {
        parseResIdFromTitle(
            context = context,
            title = data.condition,
            prefix = "bg_",
            oldSeparatedChar = '-',
            newSeparatedChar = '_'
        )
    }
    //Main container:
    Box(
        modifier = Modifier.fillMaxSize()
    ){

        Column(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(id = mainBackground),
                    contentScale = ContentScale.Crop
                )
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchBar() { location ->
                viewModel.fetchWeatherFor(location)

            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CurrentLocationButton(context, viewModel) { location ->
                    viewModel.fetchWeatherFor("${location.latitude},${location.longitude}")
                    viewModel.fetchNearbyPlaces(location.latitude, location.longitude)
                }
                LocationButton(location = data.location, locationName = data.locationName, onClick = onLocationClick)
            }
            MainInfo(
                condition = data.condition.replace('-', '_'),
                date = data.date,
                temp = data.currentTemp,
                maxTemp = data.maxTemp,
                minTemp = data.minTemp
            )
            DateContainer(date = data.date)

            Spacer(Modifier.height(30.dp))

            InfoPager(context,data, hourDatas)

            DaySwitchingButton(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                viewModel = viewModel,
                originDayIndex = viewModel.getTodayIndex() ?: 0,
                dayIndex = dayIndex,
                onPrevClick = { --dayIndex },
                onNextClick = { ++dayIndex }
            )
        }
        if (notificationMessage.value != null && notificationMessage.value != ""){
            NotificationBanner(message = notificationMessage.value)
        }
    }
}
