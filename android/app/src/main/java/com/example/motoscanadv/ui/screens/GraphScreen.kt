package com.example.motoscanadv.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motoscanadv.OBDViewModel
import java.util.Locale

data class GraphVarConfig(
    val id: String,
    val name: String,
    val unit: String,
    val min: Float,
    val max: Float,
    val color: Color,
    val label: String
)

@Composable
fun GraphScreen(viewModel: OBDViewModel) {
    val varConfigs = remember {
        listOf(
            GraphVarConfig("rpm", "RPM", "rpm", 0f, 12000f, Color(0xFF00E5FF), "RPM (rpm)"),
            GraphVarConfig("coolant", "Coolant Temperature", "°C", 0f, 150f, Color(0xFFFF1744), "Coolant Temp (°C)"),
            GraphVarConfig("speed", "Speed", "km/h", 0f, 200f, Color(0xFF00E676), "Speed (km/h)"),
            GraphVarConfig("throttle", "Throttle", "%", 0f, 100f, Color(0xFFFF9100), "Throttle Position (%)")
        )
    }

    val activeVariables = remember { mutableStateListOf("rpm", "throttle") }
    var isPlaying by remember { mutableStateOf(true) }

    // Freeze snapshot data when paused
    val frozenGraphData = remember { mutableStateListOf<Map<String, Float>>() }

    // Update frozen data when playing
    LaunchedEffect(viewModel.graphData.size, isPlaying) {
        if (isPlaying) {
            frozenGraphData.clear()
            frozenGraphData.addAll(viewModel.graphData)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .padding(16.dp)
    ) {
        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LIVE GRAPH",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "REAL-TIME MULTI-CHANNEL OSCILLOSCOPE",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // PLAY/PAUSE BUTTON
            Button(
                onClick = { isPlaying = !isPlaying },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1F26)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF263238)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPlaying) "LIVE" else "PAUSED",
                        color = Color(0xFF00E5FF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CHIPS FOR CHANNELS SELECTOR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            varConfigs.forEach { cfg ->
                val isActive = activeVariables.contains(cfg.id)
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isActive) cfg.color.copy(alpha = 0.12f) else Color(0xFF13171F),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isActive) cfg.color.copy(alpha = 0.4f) else Color(0xFF1E252E),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            if (isActive) {
                                if (activeVariables.size > 1) {
                                    activeVariables.remove(cfg.id)
                                }
                            } else {
                                activeVariables.add(cfg.id)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(cfg.color, RoundedCornerShape(3.dp))
                        )
                        Text(
                            text = cfg.name,
                            color = if (isActive) cfg.color else Color.Gray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // STACK OF CHART CONTAINERS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            varConfigs.filter { activeVariables.contains(it.id) }.forEach { cfg ->
                val currentVal = viewModel.pids.find { it.id == cfg.id }?.value ?: 0f
                OscilloscopeChartCard(
                    config = cfg,
                    currentVal = currentVal,
                    points = frozenGraphData
                )
            }
        }
    }
}

@Composable
fun OscilloscopeChartCard(
    config: GraphVarConfig,
    currentVal: Float,
    points: List<Map<String, Float>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E252E)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header stats line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = config.label.uppercase(),
                    color = config.color,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = String.format(Locale.US, "%.1f", currentVal),
                    color = config.color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Graph Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val paddingLeft = 35.dp.toPx()
                    val paddingRight = 10.dp.toPx()
                    val paddingTop = 5.dp.toPx()
                    val paddingBottom = 15.dp.toPx()

                    val chartWidth = w - paddingLeft - paddingRight
                    val chartHeight = h - paddingTop - paddingBottom

                    // Draw Horizontal Gridlines and text labels
                    val gridCount = 4
                    for (i in 0..gridCount) {
                        val ratio = i.toFloat() / gridCount
                        val y = h - paddingBottom - ratio * chartHeight
                        val valLabel = config.min + ratio * (config.max - config.min)

                        // Draw dashed line
                        drawLine(
                            color = Color(0xFF1F2937),
                            start = Offset(paddingLeft, y),
                            end = Offset(w - paddingRight, y),
                            strokeWidth = 1f
                        )

                        // Draw left text labels
                        // Note: To draw text on Canvas in Compose, we can use drawContext.canvas.nativeCanvas or keep it simple with coordinate lines.
                    }

                    // Vertical grid lines
                    val vRatios = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
                    vRatios.forEach { r ->
                        val x = paddingLeft + r * chartWidth
                        drawLine(
                            color = Color(0xFF1F2937),
                            start = Offset(x, paddingTop),
                            end = Offset(x, h - paddingBottom),
                            strokeWidth = 1f
                        )
                    }

                    // Plot sparkline path if we have points
                    if (points.size >= 2) {
                        val path = Path()
                        val fillPath = Path()
                        val totalPoints = points.size

                        points.forEachIndexed { idx, map ->
                            val divisor = if (totalPoints > 1) totalPoints - 1 else 1
                            val x = paddingLeft + (idx.toFloat() / divisor) * chartWidth
                            val rawVal = map[config.id] ?: config.min
                            val normY = ((rawVal - config.min) / (config.max - config.min)).coerceIn(0f, 1f)
                            val y = h - paddingBottom - normY * chartHeight

                            if (idx == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }

                            if (idx == totalPoints - 1) {
                                fillPath.lineTo(x, h - paddingBottom)
                                fillPath.lineTo(paddingLeft, h - paddingBottom)
                                fillPath.close()
                            }
                        }

                        // Draw area under curve gradient fill
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(config.color.copy(alpha = 0.22f), Color.Transparent),
                                startY = paddingTop,
                                endY = h - paddingBottom
                            )
                        )

                        // Draw line
                        drawPath(
                            path = path,
                            color = config.color,
                            style = Stroke(width = 1.8.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
