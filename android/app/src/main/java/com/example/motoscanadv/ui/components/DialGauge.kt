package com.example.motoscanadv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DialGauge(
    value: Float,
    min: Float,
    max: Float,
    title: String,
    unit: String,
    modifier: Modifier = Modifier,
    mainColor: Color = Color(0xFF00E5FF),
    warnThreshold: Float = 10000f,
    warnColor: Color = Color(0xFFFF1744)
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 150),
        label = "gauge"
    )

    val sweepAngle = 330f
    val startAngle = 90f // 90 degrees in Compose Canvas is exactly 6 o'clock (bottom center)
    val range = max - min
    val ratio = if (range > 0f) ((animatedValue - min) / range).coerceIn(0f, 1f) else 0f

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val widthDp = maxWidth
        val fontSizeValue = when {
            widthDp < 200.dp -> 32.sp
            widthDp > 260.dp -> 58.sp
            else -> 46.sp
        }
        val labelSizeValue = when {
            widthDp < 200.dp -> 10.sp
            widthDp > 260.dp -> 14.sp
            else -> 12.sp
        }
        val unitSizeValue = when {
            widthDp < 200.dp -> 8.sp
            widthDp > 260.dp -> 11.sp
            else -> 9.sp
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width.coerceAtMost(size.height) / 2f - 18.dp.toPx()

            // Draw 55 segmented LED blocks forming the high-tech digital gauge ring
            val totalSegments = 55
            val segmentSweep = sweepAngle / totalSegments
            val gapAngle = 1.2f
            val drawSweep = segmentSweep - gapAngle

            for (i in 0 until totalSegments) {
                val pct = i.toFloat() / (totalSegments - 1).toFloat()
                val segStartAngle = startAngle + i * segmentSweep + gapAngle / 2f

                val isActive = pct <= ratio
                val valAtSegment = min + (max - min) * pct
                val isLowLimit = warnThreshold < (min + max) / 2f
                val isWarnZone = if (isLowLimit) valAtSegment <= warnThreshold else valAtSegment >= warnThreshold

                val activeColor = if (isWarnZone) warnColor else mainColor
                val inactiveColor = if (isWarnZone) warnColor.copy(alpha = 0.12f) else mainColor.copy(alpha = 0.08f)
                val segColor = if (isActive) activeColor else inactiveColor

                drawArc(
                    color = segColor,
                    startAngle = segStartAngle,
                    sweepAngle = drawSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = if (widthDp < 200.dp) 8.dp.toPx() else 12.dp.toPx(), cap = StrokeCap.Butt)
                )
            }
        }

        // Inner Digital Readout Panel with unlit backing segments (e.g. "8888")
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                val rawText = if (title.lowercase(Locale.US).contains("rpm")) {
                    val rounded = value.toInt()
                    when {
                        rounded < 10 -> "000$rounded"
                        rounded < 100 -> "00$rounded"
                        rounded < 1000 -> "0$rounded"
                        else -> "$rounded"
                    }
                } else {
                    if (value % 1f == 0f) String.format(Locale.US, "%.0f", value) else String.format(Locale.US, "%.1f", value)
                }

                val placeholder = rawText.replace(Regex("[0-9a-zA-Z]"), "8")

                // Unlit backing segments shadow
                Text(
                    text = placeholder,
                    color = Color(0xFF1C202C).copy(alpha = 0.6f),
                    fontSize = fontSizeValue,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )

                // Glowing lit active digital numbers
                val displayColor = if (value >= warnThreshold) warnColor else mainColor
                Text(
                    text = rawText,
                    color = displayColor,
                    fontSize = fontSizeValue,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = displayColor.copy(alpha = 0.8f),
                            blurRadius = 16f
                        )
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title.uppercase(Locale.US),
                    color = Color.White,
                    fontSize = labelSizeValue,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit.lowercase(Locale.US),
                        color = Color.Gray,
                        fontSize = unitSizeValue,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}
