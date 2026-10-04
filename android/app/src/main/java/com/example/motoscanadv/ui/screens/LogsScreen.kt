package com.example.motoscanadv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motoscanadv.LogFile
import com.example.motoscanadv.OBDViewModel

@Composable
fun LogsScreen(viewModel: OBDViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .padding(16.dp)
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RIDE LOGS & HISTORY",
                    color = Color(0xFF00E5FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "OBD2 DATA LOGGER FOR ANALYSIS",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            Button(
                onClick = { viewModel.toggleRecording() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.isRecording) Color(0xFFFF1744) else Color(0xFF00E5FF)
                ),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (viewModel.isRecording) Color.White else Color.Black)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.isRecording) "STOP LOGGER" else "START LOGGER",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // RECENT LOGS LIST
        if (viewModel.logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color(0xFF1E252E), RoundedCornerShape(8.dp))
                    .background(Color(0xFF0E1117)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No saved logs found.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "Start ride logger to capture data.",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = viewModel.logs,
                    key = { it.name }
                ) { log ->
                    LogCard(
                        log = log,
                        onDelete = { viewModel.deleteLog(log.name) },
                        onToggleFav = { viewModel.toggleFavoriteLog(log.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun LogCard(
    log: LogFile,
    onDelete: () -> Unit,
    onToggleFav: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13171F)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222935)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(
                        text = "DATE: ${log.date}",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SIZE: ${log.size}",
                        color = Color(0xFF00E5FF),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onToggleFav) {
                    Icon(
                        imageVector = if (log.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (log.isFavorite) Color(0xFFFF1744) else Color.Gray
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}
