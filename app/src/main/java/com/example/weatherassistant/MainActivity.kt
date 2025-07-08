// File: MainActivity.kt
package com.example.weatherassistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherassistant.data.repository.UserPreferencesRepository
import com.example.weatherassistant.ui.theme.WeatherAssistantTheme
import com.example.weatherassistant.viewmodel.WeatherDataViewModel
import com.example.weatherassistant.viewmodel.WeatherDataViewModelFactory
import com.example.weatherassistant.data.repository.WikipediaRepository


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAssistantTheme {
                val context = LocalContext.current
                val userPrefsRepository = UserPreferencesRepository(context)
                val wikipediaRepository = WikipediaRepository()
                val viewModelFactory = WeatherDataViewModelFactory(userPrefsRepository, wikipediaRepository)
                val viewModel: WeatherDataViewModel = viewModel(factory = viewModelFactory)

                // Chỉ xử lý việc xin quyền ở đây
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1001
                    )
                }

                // Gọi đến Composable chính chứa toàn bộ giao diện và logic
                WeatherApp(viewModel = viewModel)
            }
        }
    }
}