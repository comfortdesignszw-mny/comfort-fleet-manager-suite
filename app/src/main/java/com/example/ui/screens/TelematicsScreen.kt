package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Vehicle
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.viewmodel.FleetViewModel
import com.example.ui.components.neonInteractedGlow
import com.example.ui.components.drawScrollbar
import com.example.ui.components.TelemetryTrendChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelematicsScreen(viewModel: FleetViewModel, onBack: () -> Unit) {
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    var selectedVehicleId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Telematics & Tracking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (vehicles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No vehicles found.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                contentPadding = paddingValues,
                modifier = Modifier
                    .fillMaxSize()
                    .drawScrollbar(listState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(vehicles) { vehicle ->
                    val isInteracted = selectedVehicleId == vehicle.id
                    TelematicsCard(
                        vehicle = vehicle,
                        isInteracted = isInteracted,
                        onCardClick = {
                            selectedVehicleId = if (isInteracted) null else vehicle.id
                        },
                        viewModel = viewModel,
                        onToggleImmobilizer = {
                            val newStatus = if (vehicle.status == "Immobilized") "Parked" else "Immobilized"
                            viewModel.updateVehicleStatus(vehicle, newStatus)
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun TelematicsCard(
    vehicle: Vehicle,
    isInteracted: Boolean,
    onCardClick: () -> Unit,
    viewModel: FleetViewModel,
    onToggleImmobilizer: () -> Unit
) {
    val telemetryHistory by viewModel.getTelemetryHistory(vehicle.id).collectAsStateWithLifecycle(initialValue = emptyList())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .neonInteractedGlow(isInteracted)
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                StatusIndicator(vehicle.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Reg: ${vehicle.registrationNumber}", style = MaterialTheme.typography.bodyMedium)
            
            Text("Speed: ${String.format("%.1f", vehicle.mockSpeed)} km/h", style = MaterialTheme.typography.bodyMedium)
            Text("GPS: ${String.format("%.4f", vehicle.mockLatitude)}, ${String.format("%.4f", vehicle.mockLongitude)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            
            if (isInteracted) {
                Spacer(modifier = Modifier.height(16.dp))
                TelemetryTrendChart(history = telemetryHistory)
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            val isImmobilized = vehicle.status == "Immobilized"
            Button(
                onClick = onToggleImmobilizer,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isImmobilized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isImmobilized) "Unlock Engine" else "Immobilize")
            }
        }
    }
}

@Composable
fun StatusIndicator(status: String) {
    val color = when (status) {
        "Moving" -> Color(0xFF4CAF50)
        "Started" -> Color(0xFFFFC107)
        "Immobilized" -> Color(0xFFF44336)
        else -> Color(0xFF9E9E9E)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(status, style = MaterialTheme.typography.labelMedium)
    }
}
