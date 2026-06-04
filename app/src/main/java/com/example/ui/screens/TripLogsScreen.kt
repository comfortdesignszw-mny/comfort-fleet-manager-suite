package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TripLog
import com.example.viewmodel.FleetViewModel
import com.example.ui.components.neonInteractedGlow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripLogsScreen(viewModel: FleetViewModel, onBack: () -> Unit) {
    val tripLogs by viewModel.tripLogs.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val companyProfile by viewModel.companyProfile.collectAsStateWithLifecycle()
    var showDispatchDialog by remember { mutableStateOf(false) }
    var showManualLogDialog by remember { mutableStateOf(false) }
    var returnDialogTrip by remember { mutableStateOf<TripLog?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Logs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val reportHtml = """
                            <html>
                            <head>
                            <style>
                                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 24px; color: #0A1E3F; background-color: #FFFFFF; }
                                .brand-banner { background-color: #0A1E3F; color: #FFFFFF; padding: 24px; border-radius: 12px; margin-bottom: 24px; border-left: 6px solid #00E5FF; }
                                .brand-title { color: #00E5FF; margin: 0 0 12px 0; font-size: 26px; font-weight: bold; text-transform: uppercase; letter-spacing: 1px; }
                                .brand-grid { display: table; width: 100%; margin-top: 10px; }
                                .brand-col { display: table-cell; width: 50%; vertical-align: top; font-size: 13px; line-height: 1.5; color: #E0F7FA; }
                                .brand-col strong { color: #00E5FF; font-size: 14px; }
                                h2 { font-size: 20px; color: #0A1E3F; border-bottom: 2.5px solid #00E5FF; padding-bottom: 8px; margin-top: 30px; }
                                table { width: 100%; border-collapse: collapse; margin-top: 15px; }
                                th { background-color: #0A1E3F; color: #00E5FF; padding: 12px 10px; text-align: left; font-size: 13px; font-weight: bold; border-bottom: 3px solid #00E5FF; }
                                td { padding: 10px; border-bottom: 1.5px solid #E2E8F0; font-size: 12.5px; color: #334155; }
                                tr:nth-child(even) { background-color: #F8FAFC; }
                            </style>
                            </head>
                            <body>
                            
                            <div class="brand-banner">
                                <div class="brand-title">${if (companyProfile.companyName.isNotBlank()) companyProfile.companyName else "Comfort Fleet Suite"}</div>
                                <div class="brand-grid">
                                    <div class="brand-col">
                                        <strong>Company details:</strong><br/>
                                        Reg No: ${if (companyProfile.companyNumber.isNotBlank()) companyProfile.companyNumber else "N/A"}<br/>
                                        Email: ${if (companyProfile.companyEmail.isNotBlank()) companyProfile.companyEmail else "N/A"}<br/>
                                        Address: ${if (companyProfile.companyAddress.isNotBlank()) companyProfile.companyAddress else "N/A"}
                                    </div>
                                    <div class="brand-col" style="text-align: right;">
                                        <strong>The User Details:</strong><br/>
                                        Full Name: ${if (companyProfile.contactName.isNotBlank()) companyProfile.contactName else "N/A"}<br/>
                                        Phone No: ${if (companyProfile.contactNumber.isNotBlank()) companyProfile.contactNumber else "N/A"}<br/>
                                        Email: ${if (companyProfile.contactEmail.isNotBlank()) companyProfile.contactEmail else "N/A"}
                                    </div>
                                </div>
                            </div>

                            <h2>Vehicle Trip History Report</h2>
                            <table>
                            <tr><th>Date</th><th>Vehicle</th><th>Driver</th><th>Destination</th><th>Distance Cover</th><th>Status</th></tr>
                            ${tripLogs.joinToString("") { trip ->
                                val v = vehicles.find { it.id == trip.vehicleId }
                                "<tr>" +
                                "<td>${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(trip.timeOut))}</td>" +
                                "<td>${v?.brand} ${v?.model} (${v?.registrationNumber})</td>" +
                                "<td>${trip.driverName}</td>" +
                                "<td>${trip.destination}</td>" +
                                "<td>${trip.totalMileage ?: "N/A"} km</td>" +
                                "<td>${trip.status}</td>" +
                                "</tr>"
                            }}
                            </table>
                            </body></html>
                        """.trimIndent()
                        
                        val webView = android.webkit.WebView(context)
                        webView.loadDataWithBaseURL(null, reportHtml, "text/HTML", "UTF-8", null)
                        webView.webViewClient = object : android.webkit.WebViewClient() {
                            override fun onPageFinished(view: android.webkit.WebView, url: String) {
                                val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
                                val adapter = view.createPrintDocumentAdapter("Trip_Report")
                                printManager.print("Trip Report", adapter, android.print.PrintAttributes.Builder().build())
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Export Report to PDF")
                    }
                    IconButton(onClick = {
                        val csvHeader = """
                            # --------------------------------------------------
                            # AUTHORIZED FLEET EXPORT REPORT (CSV)
                            # Company Name: ${if (companyProfile.companyName.isNotBlank()) companyProfile.companyName else "Comfort Fleet Suite"}
                            # Registration ID: ${if (companyProfile.companyNumber.isNotBlank()) companyProfile.companyNumber else "N/A"}
                            # Company Email: ${if (companyProfile.companyEmail.isNotBlank()) companyProfile.companyEmail else "N/A"}
                            # Fleet Administrator: ${if (companyProfile.contactName.isNotBlank()) companyProfile.contactName else "N/A"} (${if (companyProfile.contactEmail.isNotBlank()) companyProfile.contactEmail else "N/A"})
                            # Generated On: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
                            # --------------------------------------------------
                            
                        """.trimIndent()
                        
                        val csvRows = tripLogs.joinToString("\n") { trip ->
                            val v = vehicles.find { it.id == trip.vehicleId }
                            val vDetails = v?.let { "${it.brand} ${it.model} (${it.registrationNumber})" } ?: "Unknown"
                            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(trip.timeOut))
                            val distance = trip.totalMileage?.toString() ?: "N/A"
                            val escapedDriver = trip.driverName.replace("\"", "\"\"")
                            val escapedDest = trip.destination.replace("\"", "\"\"")
                            val fuelOutStr = trip.fuelOut ?: ""
                            val fuelInStr = trip.fuelIn ?: ""
                            "\"$dateStr\",\"$vDetails\",\"$escapedDriver\",\"$escapedDest\",\"$distance km\",\"${trip.status}\",\"$fuelOutStr\",\"$fuelInStr\""
                        }
                        
                        val totalCsv = csvHeader + "Date,Vehicle,Driver,Destination,Distance,Status,Fuel Out,Fuel In\n" + csvRows
                        
                        // Share CSV File
                        try {
                            val file = java.io.File(context.cacheDir, "Trip_History_Report.csv")
                            file.writeText(totalCsv)
                            
                            val uri: android.net.Uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "com.aistudio.comfortfleet.xjz8e.fileprovider",
                                file
                            )
                            
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Trip History Report")
                                putExtra(android.content.Intent.EXTRA_TEXT, "Completely Branded Fleet Trip Logs CSV sheet")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share CSV Report"))
                        } catch (e: Exception) {
                            // Fallback
                            val textIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, totalCsv)
                            }
                            context.startActivity(android.content.Intent.createChooser(textIntent, "Share CSV Plain Text"))
                        }
                    }) {
                        Icon(Icons.Filled.TableChart, contentDescription = "Export Report to CSV")
                    }
                    IconButton(onClick = { showManualLogDialog = true }) {
                        Icon(Icons.Filled.ListAlt, contentDescription = "Log Completed Trip")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDispatchDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Dispatch Vehicle")
            }
        }
    ) { paddingValues ->
        if (tripLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No trip logs found.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by Driver or Vehicle...") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )
                
                val filteredLogs = tripLogs.filter { trip ->
                    val vName = vehicles.find { it.id == trip.vehicleId }?.let { "${it.brand} ${it.model} ${it.registrationNumber}" } ?: ""
                    trip.driverName.contains(searchQuery, ignoreCase = true) || vName.contains(searchQuery, ignoreCase = true)
                }.sortedByDescending { it.timeOut }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(0.dp)) }
                    items(filteredLogs) { trip ->
                        val vehicleName = vehicles.find { it.id == trip.vehicleId }?.let { "${it.brand} ${it.model}" } ?: "Unknown Vehicle"
                        TripLogCard(
                        trip = trip,
                        vehicleName = vehicleName,
                        onReturn = { returnDialogTrip = trip },
                        onDelete = { viewModel.deleteTripLog(trip) },
                        onShare = {
                            val text = "Trip Log Reference:\n" +
                                    "Vehicle: $vehicleName\n" +
                                    "Driver: ${trip.driverName}\n" +
                                    "Destination: ${trip.destination}\n" +
                                    "Purpose: ${trip.tripReason}\n" +
                                    "Mileage Out: ${trip.mileageOut} km\n" +
                                    "Mileage In: ${trip.mileageIn ?: "Ongoing"}\n" +
                                    "Total Distance Covered: ${trip.totalMileage ?: "N/A"} km\n" +
                                    "Fuel Level Status: (Out) ${trip.fuelOut} -> (In) ${trip.fuelIn ?: "N/A"}"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Trip details")
                            context.startActivity(shareIntent)
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(88.dp)) } // Padding for FAB
            }
            } // Close Column
        }

        if (showDispatchDialog) {
            DispatchDialog(
                viewModel = viewModel,
                onDismiss = { showDispatchDialog = false },
                onDispatch = { vehicleId, driver, dest, reason, mileage, fuel ->
                    viewModel.dispatchVehicle(vehicleId, driver, dest, reason, mileage, fuel)
                    showDispatchDialog = false
                }
            )
        }

        if (showManualLogDialog) {
            ManualTripLogDialog(
                viewModel = viewModel,
                onDismiss = { showManualLogDialog = false },
                onLog = { vehicleId, driver, dest, reason, mileageOut, mileageIn, fuelOut, fuelIn ->
                    viewModel.logCompletedTrip(vehicleId, driver, dest, reason, mileageOut, mileageIn, fuelOut, fuelIn)
                    showManualLogDialog = false
                }
            )
        }
        
        returnDialogTrip?.let { trip ->
            ReturnDialog(
                trip = trip,
                onDismiss = { returnDialogTrip = null },
                onReturn = { mileage, fuel ->
                    viewModel.returnVehicle(trip, mileage, fuel)
                    returnDialogTrip = null
                }
            )
        }
    }
}

@Composable
fun TripLogCard(
    trip: TripLog, 
    vehicleName: String, 
    onReturn: () -> Unit, 
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isInteracted by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .neonInteractedGlow(isInteracted || isExpanded)
            .clickable { isInteracted = !isInteracted },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Driver: ${trip.driverName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Vehicle: $vehicleName", style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Badge(
                        containerColor = if (trip.status == "Active") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(trip.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = { isExpanded = !isExpanded }) {
                        Text(if (isExpanded) "Hide Specs" else "View Specs")
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Destination: ${trip.destination}", style = MaterialTheme.typography.bodyMedium)
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Full Trip Tracking Specs:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    DetailRowItem(label = "Trip Purpose", value = trip.tripReason.ifBlank { "Unspecified" })
                    DetailRowItem(label = "Time Departed", value = dateFormat.format(Date(trip.timeOut)))
                    DetailRowItem(label = "Mileage Out", value = "${trip.mileageOut} km")
                    DetailRowItem(label = "Fuel Level Out", value = trip.fuelOut)

                    if (trip.status == "Completed") {
                        DetailRowItem(label = "Time Returned", value = dateFormat.format(Date(trip.timeIn ?: 0)))
                        DetailRowItem(label = "Mileage In", value = "${trip.mileageIn} km")
                        DetailRowItem(label = "Fuel Level In", value = trip.fuelIn ?: "N/A")
                        DetailRowItem(label = "Total Distance", value = "${trip.totalMileage} km", highlight = true)
                    } else {
                        DetailRowItem(label = "Return Status", value = "In Transit / Ongoing")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Filled.Share, contentDescription = "Share details", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete entry", tint = MaterialTheme.colorScheme.error)
                    }
                }
                
                if (trip.status == "Active") {
                    Button(onClick = onReturn) {
                        Text("Return")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRowItem(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManualTripLogDialog(viewModel: FleetViewModel, onDismiss: () -> Unit, onLog: (Int, String, String, String, Int, Int, String, String) -> Unit) {
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    
    var selectedVehicleId by remember { mutableStateOf(vehicles.firstOrNull()?.id ?: 0) }
    var driver by remember { mutableStateOf("") }
    var dest by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var mileageOut by remember { mutableStateOf("") }
    var mileageIn by remember { mutableStateOf("") }
    var fuelOut by remember { mutableStateOf("Full") }
    var fuelIn by remember { mutableStateOf("Full") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Completed Trip") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (vehicles.isEmpty()) {
                    Text("No vehicles available in inventory.", color = MaterialTheme.colorScheme.error)
                } else {
                    Text("Select Vehicle:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        vehicles.forEach { v ->
                            val isSelected = selectedVehicleId == v.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedVehicleId = v.id },
                                label = { Text("${v.brand} ${v.model}") }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(value = driver, onValueChange = { driver = it }, label = { Text("Driver Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dest, onValueChange = { dest = it }, label = { Text("Destination") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Trip Purpose / Reason") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    
                    val currentVeh = vehicles.find { it.id == selectedVehicleId }
                    val defaultMileage = currentVeh?.currentMileage?.toInt()?.toString() ?: ""
                    LaunchedEffect(selectedVehicleId) {
                        if (mileageOut.isBlank() && defaultMileage.isNotBlank()) {
                            mileageOut = defaultMileage
                        }
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = mileageOut, onValueChange = { mileageOut = it }, label = { Text("Start Odometer") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = mileageIn, onValueChange = { mileageIn = it }, label = { Text("End Odometer") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = fuelOut, onValueChange = { fuelOut = it }, label = { Text("Fuel Start") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = fuelIn, onValueChange = { fuelIn = it }, label = { Text("Fuel End") }, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val miOut = mileageOut.toIntOrNull() ?: 0
                    val miIn = mileageIn.toIntOrNull() ?: 0
                    if (driver.isNotBlank() && dest.isNotBlank() && selectedVehicleId != 0 && mileageOut.isNotBlank() && mileageIn.isNotBlank()) {
                        onLog(selectedVehicleId, driver, dest, reason, miOut, miIn, fuelOut, fuelIn)
                    }
                },
                enabled = vehicles.isNotEmpty() && driver.isNotBlank() && dest.isNotBlank() && mileageOut.isNotBlank() && mileageIn.isNotBlank()
            ) { Text("Save Log") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DispatchDialog(viewModel: FleetViewModel, onDismiss: () -> Unit, onDispatch: (Int, String, String, String, Int, String) -> Unit) {
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val availableVehicles = vehicles.filter { it.status == "Parked" }
    
    var selectedVehicleId by remember { mutableStateOf(availableVehicles.firstOrNull()?.id ?: 0) }
    var driver by remember { mutableStateOf("") }
    var dest by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var fuel by remember { mutableStateOf("Full") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dispatch Vehicle") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (availableVehicles.isEmpty()) {
                    Text("No parked vehicles available in inventory.", color = MaterialTheme.colorScheme.error)
                } else {
                    Text("Select Available Vehicle:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableVehicles.forEach { v ->
                            val isSelected = selectedVehicleId == v.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedVehicleId = v.id },
                                label = { Text("${v.brand} ${v.model} (${v.registrationNumber})") }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = driver, onValueChange = { driver = it }, label = { Text("Driver Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dest, onValueChange = { dest = it }, label = { Text("Destination") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Trip Purpose / Reason") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    
                    val currentVeh = availableVehicles.find { it.id == selectedVehicleId }
                    val defaultMileage = currentVeh?.currentMileage?.toInt()?.toString() ?: ""
                    LaunchedEffect(selectedVehicleId) {
                        if (mileage.isBlank() && defaultMileage.isNotBlank()) {
                            mileage = defaultMileage
                        }
                    }
                    
                    OutlinedTextField(
                        value = mileage, 
                        onValueChange = { mileage = it }, 
                        label = { Text("Mileage Out (Start)") }, 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    OutlinedTextField(value = fuel, onValueChange = { fuel = it }, label = { Text("Fuel Out (Start)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val mi = mileage.toIntOrNull() ?: 0
                    if (driver.isNotBlank() && dest.isNotBlank() && selectedVehicleId != 0) {
                        onDispatch(selectedVehicleId, driver, dest, reason, mi, fuel)
                    }
                },
                enabled = availableVehicles.isNotEmpty() && driver.isNotBlank() && dest.isNotBlank()
            ) { Text("Dispatch") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ReturnDialog(trip: TripLog, onDismiss: () -> Unit, onReturn: (Int, String) -> Unit) {
    var mileage by remember { mutableStateOf("") }
    var fuel by remember { mutableStateOf("Full") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Return Vehicle") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Returning of Vehicle from: ${trip.destination}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = mileage, onValueChange = { mileage = it }, label = { Text("Mileage In (Current)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fuel, onValueChange = { fuel = it }, label = { Text("Fuel Level In (Full, 1/2, Empty)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val mi = mileage.toIntOrNull() ?: 0
                    if (mileage.isNotBlank()) {
                        onReturn(mi, fuel)
                    }
                },
                enabled = mileage.isNotBlank()
            ) { Text("Confirm Return") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
