package com.example.weatherassistant.views.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp





@Composable
fun SearchBar(modifier: Modifier = Modifier, onSearching: (String) -> Unit){
    var locationInput by remember { mutableStateOf("") }
    OutlinedTextField(
        value = locationInput,
        onValueChange = { locationInput = it},
        placeholder = {
            Row(){
                Text(
                    text = "Input your lacation",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray

                )
            }
        },

        textStyle = androidx.compose.ui.text.TextStyle(
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black,
            fontSize = 20.sp
        ),

        trailingIcon = {
            var isPressed by remember { mutableStateOf(false) }

            IconButton(
                onClick = {
                    isPressed = true
                    onSearching(locationInput.trim())
                },
                modifier = Modifier
                    .size(40.dp)
                    .then(
                        if (isPressed) Modifier.padding(2.dp) else Modifier
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "search",
                    tint = if (isPressed) Color(0xFF64B5F6) else Color(0xFF2196F3),
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        },
                modifier = modifier
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp),
        shape = RoundedCornerShape(percent = 20),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.8f),   // nhẹ trong suốt
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f), // rất nhẹ
            focusedBorderColor = Color.White.copy(alpha = 0.4f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.Black
        )
    )
}

@Composable
fun LocationButton(modifier: Modifier = Modifier, location: String, locationName: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)

            .clickable(onClick = { onClick() }),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(50.dp).padding(end = 10.dp),
            tint = Color.Red
        )
        Text(
            text = if (location.isEmpty() && locationName.isNullOrEmpty()) "No location set" else locationName ?: location,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 25.sp,
            modifier = Modifier
                .background(color = Color.White.copy(alpha = 0.001f)) // nền trắng mờ
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BackgroundContainer(title: String, modifier: Modifier = Modifier, components: @Composable () -> Unit){
    val context = LocalContext.current

    val bgResId = parseResIdFromTitle(context = context, title = title, prefix = "bg_", oldSeparatedChar = '-', newSeparatedChar = '_')

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Image(
            painter = painterResource(bgResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            components
        }
    }
}