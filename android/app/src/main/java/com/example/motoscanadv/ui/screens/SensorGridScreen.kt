package com.example.motoscanadv.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motoscanadv.OBDViewModel
import com.example.motoscanadv.PID

@Composable
fun SensorGridScreen(viewModel: OBDViewModel) {
    LaunchedEffect(Unit) {
        viewModel.activeSensorIds = emptySet()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .padding(16.dp)
    ) {
        Text(
            text = "ALL OBD2 SENSOR MONITOR",
            color = Color(0xFF00E5FF),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // SENSORS GRID
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1.3f)
        ) {
            items(
                items = viewModel.pids,
                key = { it.id }
            ) { pid ->
                SensorGridCard(pid = pid)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // REAL-TIME GRAPH VISUALIZER FOR SENSOR FEED - Optimized separate scope
        LiveGraphCard(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
        )
    }
}

@Composable
fun LiveGraphCard(viewModel: OBDViewModel, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232B35)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "LIVE GRAPH FEED (RPM vs THROTTLE)",
                color = Color.LightGray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Draw manual high fidelity Sparkline Plot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val points = viewModel.graphData

                    if (points.isNotEmpty()) {
                        val rpmPath = Path()
                        val tPath = Path()
                        val totalPoints = points.size

                        points.forEachIndexed { index, map ->
                            val divisor = if (totalPoints > 1) totalPoints - 1 else 1
                            val x = (index.toFloat() / divisor) * width
                            val rpm = map["rpm"] ?: 1500f
                            val throttle = map["throttle"] ?: 0f

                            // Normalize safely
                            val normRpm = ((rpm - 1000f) / 10500f).coerceIn(0f, 1f)
                            val normThrottle = (throttle / 100f).coerceIn(0f, 1f)

                            val yRpm = height - normRpm * height
                            val yThrottle = height - normThrottle * height

                            if (index == 0) {
                                rpmPath.moveTo(x, yRpm)
                                tPath.moveTo(x, yThrottle)
                            } else {
                                rpmPath.lineTo(x, yRpm)
                                tPath.lineTo(x, yThrottle)
                            }
                        }

                        // Draw Lines
                        drawPath(
                            path = rpmPath,
                            color = Color(0xFF00E5FF),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawPath(
                            path = tPath,
                            color = Color(0xFFFF9100),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF00E5FF)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RPM (Cyan)", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFFFF9100)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Throttle (Orange)", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun SensorGridCard(pid: PID) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13171F)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222935)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = pid.shortName.uppercase(),
                color = Color.Gray,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", pid.value),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = pid.unit,
                    color = Color(0xFF00E5FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            // Mini progress visualizer track
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0xFF1C222C))
            ) {
                val range = pid.max - pid.min
                val ratio = if (range > 0f) ((pid.value - pid.min) / range).coerceIn(0f, 1f) else 0f
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(ratio)
                        .background(Color(0xFF00E5FF))
                )
            }
        }
    }
}
