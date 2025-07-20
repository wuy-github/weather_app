// File: MainActivity.kt
package com.example.weatherassistant

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherassistant.Utils.worker.scheduleWeatherWorker
import com.example.weatherassistant.data.remote.saveFCMTokenAndLocationToFirestore
import com.example.weatherassistant.data.repository.UserPreferencesRepository
import com.example.weatherassistant.ui.theme.WeatherAssistantTheme
import com.example.weatherassistant.viewmodel.WeatherDataViewModel
import com.example.weatherassistant.viewmodel.factory.WeatherDataViewModelFactory
import com.example.weatherassistant.data.repository.WikipediaRepository
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.WellKnownTileServer


class MainActivity : ComponentActivity() {
    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        val requestPermission = registerForActivityResult (
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted)
                Log.d("NotificationPermission", "☻ Notification is permitted")
            else
                Log.d("NotificationPermission", "♠ Notification permission was denied")
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Request Notification Posting Permissionhere:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val deviceToken = mutableStateOf<String?> (null)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            deviceToken.value = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, deviceToken)
            Log.d("TAG", "\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25 The Token is: ${deviceToken.value}")
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
        FirebaseMessaging.getInstance().subscribeToTopic("weather_alert")

        // Fake MapBox API Key to pass
        Mapbox.getInstance(this, "dummy-key", WellKnownTileServer.MapLibre) // Không cần thật, chỉ để vượt qua check
        setContent {
            WeatherAssistantTheme {
                val title = intent.getStringExtra("title")
                val body = intent.getStringExtra("body")

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
                WeatherApp(viewModel = viewModel, title, body, deviceToken.value)
            }
        }
        scheduleWeatherWorker(applicationContext)
    }
}