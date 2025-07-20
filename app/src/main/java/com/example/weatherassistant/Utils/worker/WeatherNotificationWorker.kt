package com.example.weatherassistant.Utils.worker

import android.app.*
import android.content.*
import android.os.Build
import androidx.work.Constraints
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.weatherassistant.MainActivity
import com.example.weatherassistant.R
import com.example.weatherassistant.Utils.Notification.getWeatherAlerts
import com.example.weatherassistant.data.repository.WeatherFlashDataRepository
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit


fun scheduleWeatherWorker(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<WeatherNotificationWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(calculateInitialTimeDelay(7), TimeUnit.MINUTES)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_weather",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}

fun calculateInitialTimeDelay(hour: Int): Long {
    val now = LocalDateTime.now()
    val target = now.withHour(hour).withMinute(0).withSecond(0)
    return Duration.between(now, if (now < target) target else target.plusDays(1)).toMinutes()
}

class WeatherNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    private val repository = WeatherFlashDataRepository(context)

    override fun doWork(): Result {
        val hourlyData = repository.readHourlyDataFromPrefs()
        val alertMessage = getWeatherAlerts(hourlyData ?: return Result.failure())

        val title =
            if (!alertMessage.isNullOrEmpty()) "⛅ Cảnh báo thời tiết" else "⛅ Dự báo hôm nay"
        val body = alertMessage ?: "Thời tiết hôm nay ổn định."

        showNotification(applicationContext, title, body)

        return Result.success()
    }

    private fun showNotification(context: Context, title: String, body: String) {
        val channelId = "default_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Thông báo chung",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("title", title)
            putExtra("body", body)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_ic_2)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}

/*
fun scheduleWeatherWorker(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<WeatherNotificationWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(calculateInitialDelay(7), TimeUnit.MINUTES) // 07:00 sáng
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_weather",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}

fun calculateInitialDelay(targetHour: Int): Long {
    val now = LocalDateTime.now()
    val target = now.withHour(targetHour).withMinute(0).withSecond(0)
    return Duration.between(now, if (now < target) target else target.plusDays(1)).toMinutes()
}


class WeatherNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    val repository = WeatherFlashDataRepository(context)

    override fun doWork(): Result {
        val hourlyData = repository.readHourlyDataFromPrefs()
        val alertMessage = getWeatherAlerts(hourlyData ?: return Result.failure())

        val title = "Du bao thoi tiet hom nay"
        val body = if (!alertMessage.isNullOrEmpty()) alertMessage else "Thoi tiet on dinh"

        showNotification(applicationContext, title, body)

        return Result.success()
    }

    private fun showNotification(context: Context, title: String, body: String) {
        val channelId = "default_channel"
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "channel_default_name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("title", title)
            putExtra("body", body)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_ic_2)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(0, notification)
    }
}

*/
