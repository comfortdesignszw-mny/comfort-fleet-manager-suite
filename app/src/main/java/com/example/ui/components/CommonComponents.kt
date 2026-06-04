package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.TelemetryHistory

@Composable
fun Modifier.neonInteractedGlow(
    isInteracted: Boolean,
    glowColor: Color = Color(0xFF00E5FF)
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isInteracted) 1.025f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isInteracted) 0.85f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "glowAlpha"
    )

    // Pulsing outline stroke width
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val strokeWidthPulse by infiniteTransition.animateFloat(
        initialValue = 1.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseStroke"
    )

    return this
        .scale(scale)
        .drawBehind {
            if (glowAlpha > 0f) {
                // Glow boundary shadow
                drawRoundRect(
                    color = glowColor.copy(alpha = glowAlpha * 0.25f),
                    size = size,
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                    style = Stroke(width = strokeWidthPulse.dp.toPx() * 1.6f)
                )
                // Sharp neon edge
                drawRoundRect(
                    color = glowColor.copy(alpha = glowAlpha),
                    size = size,
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                    style = Stroke(width = strokeWidthPulse.dp.toPx())
                )
            }
        }
}

@Composable
fun TelemetryTrendChart(history: List<TelemetryHistory>, modifier: Modifier = Modifier) {
    if (history.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color(0xFF12121A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Awaiting live telematics updates from simulator...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        return
    }

    // Capture last 10 updates and sort chronologically (oldest to newest for graph flow)
    val points = history.take(10).reversed()

    val maxSpeed = 100f
    val maxFuel = 100f
    
    val mileages = points.map { it.mileage }
    val minMileage = mileages.minOrNull() ?: 0f
    val maxMileage = mileages.maxOrNull() ?: (minMileage + 10f)
    val mileageRange = (maxMileage - minMileage).coerceAtLeast(1f)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111219)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Telematics Analytics Trend (Last 10 updates)",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Graph canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val width = size.width
                val height = size.height
                val totalPoints = points.size

                // Draw background grid lines
                for (i in 1..3) {
                    val gridY = height * (i * 0.25f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = androidx.compose.ui.geometry.Offset(0f, gridY),
                        end = androidx.compose.ui.geometry.Offset(width, gridY),
                        strokeWidth = 1f
                    )
                }

                if (totalPoints >= 2) {
                    val stepX = width / (totalPoints - 1)

                    // Helper to project and draw a line plot
                    fun drawPropertyLine(
                        color: Color,
                        minVal: Float,
                        maxVal: Float,
                        extractor: (TelemetryHistory) -> Float
                    ) {
                        val path = androidx.compose.ui.graphics.Path()
                        points.forEachIndexed { i, p ->
                            val value = extractor(p)
                            val normY = ((value - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                            val x = i * stepX
                            val y = height - (normY * (height - 30f) + 15f) // top/bottom padding

                            if (i == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        // Drawing line
                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(width = 3f)
                        )

                        // Outer indicators
                        points.forEachIndexed { i, p ->
                            val value = extractor(p)
                            val normY = ((value - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                            val x = i * stepX
                            val y = height - (normY * (height - 30f) + 15f)

                            drawCircle(
                                color = color,
                                radius = 4f,
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                            drawCircle(
                                color = color.copy(alpha = 0.25f),
                                radius = 8f,
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                        }
                    }

                    // 1. Speed line (Orange-Red)
                    drawPropertyLine(Color(0xFFFFA726), 0f, maxSpeed) { it.speed }

                    // 2. Fuel line (Green)
                    drawPropertyLine(Color(0xFF66BB6A), 0f, maxFuel) { it.fuelLevel }

                    // 3. Mileage line (Neon Cyan)
                    drawPropertyLine(Color(0xFF00E5FF), minMileage, maxMileage) { it.mileage }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendIndicatorItem(color = Color(0xFFFFA726), label = "Speed (km/h)")
                LegendIndicatorItem(color = Color(0xFF66BB6A), label = "Fuel (%)")
                LegendIndicatorItem(color = Color(0xFF00E5FF), label = "Mileage (km)")
            }
        }
    }
}

@Composable
fun LegendIndicatorItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.bodySmall, 
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
