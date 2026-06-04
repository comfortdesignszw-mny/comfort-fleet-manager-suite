package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.FleetViewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: FleetViewModel) {
    val tripLogs by viewModel.tripLogs.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.DirectionsCar, contentDescription = null, modifier = Modifier.size(24.dp))
                            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp).offset(x = 6.dp, y = (-6).dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Comfort Fleet Manager Suite", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Fleet Overview Stats
            val activeFleetCount = vehicles.count { it.status != "Immobilized" }
            val dueForServiceCount = vehicles.count { it.currentMileage >= it.serviceThreshold }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Active Fleet", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("$activeFleetCount", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Due for Service", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("$dueForServiceCount", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Total Distance Traveled (Last 30 Days)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Calculate distance per vehicle
            val now = System.currentTimeMillis()
            val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
            val recentTrips = tripLogs.filter { it.status == "Completed" && (now - (it.timeIn ?: 0)) <= thirtyDaysMs }
            
            val distancePerVehicle = recentTrips.groupBy { it.vehicleId }
                .mapValues { entry -> entry.value.sumOf { trip -> trip.totalMileage ?: 0 } }
            
            if (distancePerVehicle.isEmpty()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp), contentAlignment = Alignment.Center) {
                    Text("No completed trip data for the last 30 days.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(300.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        DistanceBarChart(distancePerVehicle = distancePerVehicle, vehicles = vehicles)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Average Trip Distance per Driver",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val driverStats = recentTrips.groupBy { it.driverName }.mapValues { entry -> 
                val trips = entry.value
                val avgDistance = if (trips.isNotEmpty()) trips.sumOf { it.totalMileage ?: 0 } / trips.size else 0
                Pair(avgDistance, trips.size)
            }
            
            if (driverStats.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No driver data for the last 30 days.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                driverStats.entries.sortedByDescending { it.value.first }.forEach { (driver, stats) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(text = driver, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = "${stats.second} Trips Logged", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(text = "${stats.first} km avg", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Weekly Distance per Vehicle (Last 7 Days)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
            val recentTripsWeek = tripLogs.filter { it.status == "Completed" && (now - (it.timeIn ?: 0)) <= sevenDaysMs }
            
            val distancePerVehicleWeek = recentTripsWeek.groupBy { it.vehicleId }
                .mapValues { entry -> entry.value.sumOf { trip -> trip.totalMileage ?: 0 }.toFloat() }
                
            if (distancePerVehicleWeek.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No completed trip data for the last 7 days.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(250.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        DonutChart(distances = distancePerVehicleWeek, vehicles = vehicles)
                    }
                }
            }

            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
fun DonutChart(distances: Map<Int, Float>, vehicles: List<com.example.data.Vehicle>) {
    val total = distances.values.sum()
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        Color(0xFF009688),
        Color(0xFFFF9800),
        Color(0xFF673AB7)
    )
    
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 40.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        
        var startAngle = -90f
        var colorIndex = 0
        
        distances.forEach { (vehicleId, distance) ->
            val sweepAngle = (distance / total) * 360f
            val vehicle = vehicles.find { it.id == vehicleId }
            val brand = vehicle?.brand ?: "Unknown"
            
            drawArc(
                color = colors[colorIndex % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
            
            // Text in center (total)
            drawText(
                textMeasurer = textMeasurer,
                text = "${total.toInt()} km",
                topLeft = Offset(size.width / 2f - 30.dp.toPx(), size.height / 2f - 15.dp.toPx()),
                style = androidx.compose.ui.text.TextStyle(
                    color = onSurfaceColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            
            startAngle += sweepAngle
            colorIndex++
        }
    }
}

@Composable
fun DistanceBarChart(distancePerVehicle: Map<Int, Int>, vehicles: List<com.example.data.Vehicle>) {
    val maxDistance = distancePerVehicle.values.maxOrNull()?.toFloat() ?: 1f
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        val bottomPadding = 48.dp.toPx()
        val topPadding = 20.dp.toPx()
        val graphHeight = height - bottomPadding - topPadding
        
        // Draw grid lines
        for (i in 0..4) {
            val y = topPadding + graphHeight - (i * (graphHeight / 4))
            drawLine(
                color = onSurfaceColor.copy(alpha = 0.2f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            // Value text
            val value = (maxDistance * (i / 4f)).toInt()
            drawText(
                textMeasurer = textMeasurer,
                text = "$value km",
                topLeft = Offset(0f, y - 20.dp.toPx()),
                style = androidx.compose.ui.text.TextStyle(color = onSurfaceColor, fontSize = 10.sp)
            )
        }
        
        val barCount = distancePerVehicle.size
        val barWidth = (width / (barCount * 2f)).coerceAtMost(60.dp.toPx())
        val spacing = (width - (barWidth * barCount)) / (barCount + 1f)
        
        var currentX = spacing
        
        distancePerVehicle.entries.forEach { (vehicleId, distance) ->
            val vehicle = vehicles.find { it.id == vehicleId }
            val brand = vehicle?.brand ?: "Unknown"
            
            val barHeight = (distance.toFloat() / maxDistance) * graphHeight
            val barTop = topPadding + graphHeight - barHeight
            
            drawRoundRect(
                brush = SolidColor(primaryColor),
                topLeft = Offset(currentX, barTop),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            
            drawText(
                textMeasurer = textMeasurer,
                text = brand.take(8), // Shorten label
                topLeft = Offset(currentX, height - 30.dp.toPx()),
                style = androidx.compose.ui.text.TextStyle(color = onSurfaceColor, fontSize = 10.sp)
            )
            
            currentX += barWidth + spacing
        }
    }
}
