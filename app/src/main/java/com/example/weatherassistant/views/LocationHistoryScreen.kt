package com.example.weatherassistant.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherassistant.R
import com.example.weatherassistant.viewmodel.WeatherDataViewModel

// Danh sách hình nền
private val backgroundList = listOf(
    R.drawable.bg_clear_day,
    R.drawable.bg_clear_night,
    R.drawable.bg_cloudy,
    R.drawable.bg_rain,
    R.drawable.bg_snow,
    R.drawable.bg_sunny,
    R.drawable.bg_partly_cloudy_day,
    R.drawable.bg_partly_cloudy_night,
    R.drawable.bg_error,
    R.drawable.bg_fog,
    R.drawable.bg_loading_screen
)

// Màu nền của các card
private val cardColorList = listOf(
    Color(0xFFB2EBF2),
    Color(0xFFC8E6C9),
    Color(0xFFFFF9C4),
    Color(0xFFF8BBD0),
    Color(0xFFD1C4E9),
    Color(0xFFFFE0B2)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationHistoryScreen(
    viewModel: WeatherDataViewModel,
    onNavigateBack: () -> Unit,
    onHistoryItemClick: (String) -> Unit
) {
    var showHistoryList by remember { mutableStateOf(false) }
    val randomBackground = remember { backgroundList.random() }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = randomBackground),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (showHistoryList) "Lịch sử tìm kiếm" else "Trợ lý chức năng",
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (showHistoryList) showHistoryList = false else onNavigateBack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (showHistoryList) {
                            IconButton(onClick = { viewModel.clearSearchHistory() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Xóa lịch sử",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.3f)
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (showHistoryList) {
                    HistoryList(viewModel = viewModel, onHistoryItemClick = onHistoryItemClick)
                } else {
                    OptionGrid(onShowHistoryClick = { showHistoryList = true })
                }
            }
        }
    }
}

@Composable
private fun OptionGrid(onShowHistoryClick: () -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OptionCard(
                title = "Lịch sử",
                icon = Icons.Default.History,
                onClick = onShowHistoryClick
            )
        }
        item {
            OptionCard(
                title = "Tính năng khác",
                icon = Icons.Default.Star,
                onClick = { /* TODO: future feature */ }
            )
        }
    }
}

@Composable
private fun OptionCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    val randomColor = remember { cardColorList.random() }
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = randomColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = Color.Black.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 18.sp, color = Color.Black.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun HistoryList(viewModel: WeatherDataViewModel, onHistoryItemClick: (String) -> Unit) {
    val history by viewModel.searchHistory.collectAsState()

    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Chưa có lịch sử tìm kiếm.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                "Lịch sử gần đây",
                style = MaterialTheme.typography.headlineSmall.copy(color = Color.White)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn {
                items(history.toList().reversed()) { location ->
                    HistoryItemCard(location = location) {
                        onHistoryItemClick(location)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(location: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = location,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontSize = 18.sp
                )
            )
        }
    }
}
