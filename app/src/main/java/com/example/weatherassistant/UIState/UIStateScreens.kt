package com.example.weatherassistant.UIState

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(){
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        if(!loading) {
            Button(onClick = {loading = true}, enabled = !loading) { Text("Start Loading")}
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(100.dp),
                color = Color(0xFF003300),
                trackColor = Color(0xFFCC33DD),
                strokeWidth = 5.dp
            )
        }
    }
}

@Composable
fun ErrorScreen(message: String){
    Column(
        modifier = Modifier.fillMaxSize().background(color = Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Lỗi lấy dữ liệu: $message",
            color = Color.Red,
            fontSize = 25.sp
        )
    }
}

