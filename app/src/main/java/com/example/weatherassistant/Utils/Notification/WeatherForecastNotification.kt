package com.example.weatherassistant.Utils.Notification

import com.example.weatherassistant.data.model.HourlyData
import com.example.weatherassistant.viewmodel.WeatherFlashViewModel

fun weatherForecastNotification(viewModel: WeatherFlashViewModel): String? {
    val data = viewModel.wholeResponseData.value
    return data?.let { data ->
        val todayHourlyData = data.hourly.copy (
            temperature_2m = data.hourly.temperature_2m.take(24),
            rain = data.hourly.rain.take(24),
            precipitation = data.hourly.precipitation.take(24),
            cloud_cover = data.hourly.cloud_cover.take(24),
            wind_gusts_10m = data.hourly.wind_gusts_10m.take(24),
            wind_speed_10m = data.hourly.wind_speed_10m.take(24),
            wind_direction_10m = data.hourly.wind_direction_10m.take(24),
            weather_code = data.hourly.weather_code.take(24)
            )
        getWeatherAlerts(todayHourlyData)
    }
}
fun getWeatherAlerts(hourlyData: HourlyData): String? {
    val alerts = mutableSetOf<String>()

    val firstRainHour = (0..23).firstOrNull{
        hourlyData.precipitation[it] > 0.0
    }
    if (firstRainHour != null) {
        alerts.add("☔ Trời có mưa lúc $firstRainHour:00")
    }

    if (hourlyData.weather_code.any {it in listOf(95, 96, 99)}) {
        alerts.add("⚡ Có thể có giông – tránh ở ngoài trời lâu.")
    }

    if (hourlyData.temperature_2m.any {it > 36}) {
        alerts.add("\uD83D\uDD25 Nắng nóng – hạn chế làm việc ngoài trời buổi trưa.")
    }

    return if (alerts.isNotEmpty()) alerts.joinToString("\n") else null
}

// Có thể phát triển thêm bằng việc tổ thức một thông báo lấy từ 3 hàm thông báo:
    // + weatherAlerts(hourlyData: HourlyData): String => Cảnh báo thời tiết không tốt trong một khoảng thời gian
    // + alertActivitiesToAvoid(hourlyData: HourlyData): String => Cảnh báo hành động cần tránh
    // + convenientWorkingHours(hourlyData: HourlyData): String => Gợi ý khung giờ phù hợp cho các hoạt động
fun translateWeatherCode(code: Int): String = when(code) {
    0  -> "Trời quang đãng"
    1  -> "Ít mây"
    2  -> "Mây vừa"
    3  -> "Nhiều mây / U ám"
    45 -> "Sương mù nhẹ"
    48 -> "Sương mù dày đặc"

    51 -> "Mưa phùn nhẹ"
    53 -> "Mưa phùn vừa"
    55 -> "Mưa phùn nặng"
    56 -> "Mưa phùn + băng giá nhẹ"
    57 -> "Mưa phùn + băng giá mạnh"

    61 -> "Mưa nhẹ"
    63 -> "Mưa vừa"
    65 -> "Mưa lớn"
    66 -> "Mưa lạnh nhẹ"
    67 -> "Mưa lạnh mạnh"

    71 -> "Tuyết nhẹ"
    73 -> "Tuyết vừa"
    75 -> "Tuyết lớn"
    77 -> "Tuyết dạng viên đá nhỏ"

    80 -> "Mưa rào nhẹ"
    81 -> "Mưa rào vừa"
    82 -> "Mưa rào lớn"

    85 -> "Tuyết rào nhẹ"
    86 -> "Tuyết rào lớn"

    95 -> "Dông nhẹ hoặc vừa"
    96 -> "Dông + mưa đá nhẹ"
    99 -> "Dông + mưa đá mạnh"

    else -> "Không xác định / Không có dữ liệu"
}
