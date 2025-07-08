package com.example.weatherassistant.views.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherassistant.viewmodel.WeatherDataViewModel

@Composable
fun DaySwitchingButton(modifier: Modifier = Modifier, originDayIndex: Int, dayIndex: Int, viewModel: WeatherDataViewModel = viewModel(), onPrevClick: () -> Unit, onNextClick: () -> Unit ) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = {
                onPrevClick()
             },
            enabled = dayIndex != originDayIndex,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x8800EFD5),
                contentColor = Color(0xFF005F45)
            ),
            contentPadding = PaddingValues(5.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("1 ngày trước")
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                onNextClick()
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x8800EFD5),
                contentColor = Color(0xFF005F45)
            ),
            contentPadding = PaddingValues(5.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("1 ngày sau")
        }
    }
}