package com.example.weatherassistant.views.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun NotificationBanner(message: String?, modifier: Modifier = Modifier){
    val visibleState = remember { MutableTransitionState(false) }

    LaunchedEffect(message) {
        visibleState.targetState = true
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = slideInVertically() { -it } + fadeIn(),
        exit = slideOutVertically() { -it } + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .zIndex(1f)
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF333333), shape = RoundedCornerShape(8.dp))
                .padding(vertical = 10.dp, horizontal = 16.dp)
        ) {
            Text(
                text = message ?: "",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}