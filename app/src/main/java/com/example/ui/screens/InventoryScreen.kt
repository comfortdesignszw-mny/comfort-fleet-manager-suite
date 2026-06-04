package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TableChart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.data.Vehicle
import com.example.viewmodel.FleetViewModel
import com.example.ui.components.neonInteractedGlow
import com.example.ui.components.drawScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: FleetViewModel, onBack: () -> Unit) {
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val companyProfile by viewModel.companyProfile.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editVehicleTarget by remember { mutableStateOf<Vehicle?>(null) }
    var serviceHistoryTarget by remember { mutableStateOf<Vehicle?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehicle Inventory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                },
                actions = {
                    val context = LocalContext.current
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

                            <h2>Vehicle Inventory Report</h2>
                            <table>
                            <tr><th>Brand & Model</th><th>Registration</th><th>Type</th><th>Condition</th><th>Mileage</th><th>Status</th></tr>
                            ${vehicles.joinToString("") { v ->
                                "<tr>" +
                                "<td>${v.brand} ${v.model}</td>" +
                                "<td>${v.registrationNumber}</td>" +
                                "<td>${v.type}</td>" +
                                "<td>${v.condition}</td>" +
                                "<td>${String.format(Locale.getDefault(), "%.1f", v.currentMileage)} km</td>" +
                                "<td>${v.status}</td>" +
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
                                val adapter = view.createPrintDocumentAdapter("Inventory_Report")
                                printManager.print("Inventory Report", adapter, android.print.PrintAttributes.Builder().build())
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Export Inventory to PDF")
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
                        
                        val csvRows = vehicles.joinToString("\n") { v ->
                            val brandModel = "${v.brand} ${v.model}".replace("\"", "\"\"")
                            val regNo = v.registrationNumber.replace("\"", "\"\"")
                            val currentMileageStr = String.format(Locale.getDefault(), "%.1f km", v.currentMileage)
                            "\"$brandModel\",\"$regNo\",\"${v.type}\",\"${v.condition}\",\"$currentMileageStr\",\"${v.status}\""
                        }
                        
                        val totalCsv = csvHeader + "Brand & Model,Registration,Type,Condition,Mileage,Status\n" + csvRows
                        
                        // Share CSV File
                        try {
                            val file = java.io.File(context.cacheDir, "Vehicle_Inventory_Report.csv")
                            file.writeText(totalCsv)
                            
                            val uri: android.net.Uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "com.aistudio.comfortfleet.xjz8e.fileprovider",
                                file
                            )
                            
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Vehicle Inventory Report")
                                putExtra(android.content.Intent.EXTRA_TEXT, "Completely Branded Fleet Vehicle Inventory CSV sheet")
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
                        Icon(Icons.Filled.TableChart, contentDescription = "Export Inventory to CSV")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Vehicle")
            }
        }
    ) { paddingValues ->
        if (vehicles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No vehicles in inventory.", style = MaterialTheme.typography.bodyLarge)
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
                    VehicleCard(
                        vehicle = vehicle,
                        onDelete = { viewModel.deleteVehicle(vehicle) },
                        onEdit = { editVehicleTarget = vehicle },
                        onViewServiceHistory = { serviceHistoryTarget = vehicle }
                    )
                }
                item { Spacer(modifier = Modifier.height(88.dp)) } // Padding for FAB
            }
        }

        if (showAddDialog) {
            AddVehicleDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { brand, model, type, regNo, condition, mileage, threshold ->
                    val mValue = mileage.toFloatOrNull() ?: 0f
                    val tValue = threshold.toFloatOrNull() ?: 15000f
                    viewModel.addVehicle(brand, model, type, regNo, condition, mValue, tValue)
                    showAddDialog = false
                }
            )
        }

        editVehicleTarget?.let { target ->
            EditVehicleDialog(
                vehicle = target,
                onDismiss = { editVehicleTarget = null },
                onUpdate = { updated ->
                    viewModel.updateVehicle(updated)
                    editVehicleTarget = null
                }
            )
        }

        serviceHistoryTarget?.let { target ->
            ServiceHistoryDialog(
                vehicle = target,
                viewModel = viewModel,
                onDismiss = { serviceHistoryTarget = null }
            )
        }
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onViewServiceHistory: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isInteracted by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isDueForService = vehicle.currentMileage >= vehicle.serviceThreshold

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .neonInteractedGlow(isInteracted || isExpanded)
            .clickable { isInteracted = !isInteracted },
        colors = CardDefaults.cardColors(
            containerColor = if (isDueForService) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Service Warning top indicator
            if (isDueForService) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = ShapeDefaults.Small,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info, 
                            contentDescription = "Service Triggered", 
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "⚠️ Due for Service (Threshold Exceeded)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${vehicle.brand} ${vehicle.model}", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reg: ${vehicle.registrationNumber} • Type: ${vehicle.type}", 
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // View/Expand Button alongside Delete/Edit
                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Text(if (isExpanded) "Hide Specs" else "View Specs")
                }
            }

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
                        text = "Full Vehicle Details:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    DetailRow(label = "Condition", value = vehicle.condition)
                    DetailRow(label = "Current Status", value = vehicle.status)
                    DetailRow(label = "Current Mileage", value = "${String.format("%.1f", vehicle.currentMileage)} km")
                    DetailRow(label = "Service Threshold", value = "${String.format("%.1f", vehicle.serviceThreshold)} km")
                    DetailRow(label = "Fuel Level", value = "${String.format("%.1f", vehicle.fuelLevel)}%")

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Standard controls row (Delete, Edit, Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onViewServiceHistory) {
                    Icon(Icons.Filled.ListAlt, contentDescription = "Service Records", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = {
                    val shareText = "Vehicle Information:\n" +
                            "Make/Model: ${vehicle.brand} ${vehicle.model}\n" +
                            "Registration: ${vehicle.registrationNumber}\n" +
                            "Type: ${vehicle.type}\n" +
                            "Condition: ${vehicle.condition}\n" +
                            "Status: ${vehicle.status}\n" +
                            "Mileage: ${String.format("%.1f", vehicle.currentMileage)} km\n" +
                            "Next Service threshold: ${String.format("%.1f", vehicle.serviceThreshold)} km\n" +
                            "Fuel: ${String.format("%.1f", vehicle.fuelLevel)}%"
                    
                    try {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Fleet Vehicle Details")
                        context.startActivity(shareIntent)
                    } catch (e: Exception) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Vehicle Info", shareText)
                        clipboard.setPrimaryClip(clip)
                    }
                }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share Specs", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Vehicle", tint = MaterialTheme.colorScheme.secondary)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Vehicle", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, String, String) -> Unit
) {
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var registration by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("SUV") }
    var condition by remember { mutableStateOf("Good") }
    var mileage by remember { mutableStateOf("10000") }
    var threshold by remember { mutableStateOf("15000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Vehicle Entry") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand/Make") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = registration, onValueChange = { registration = it }, label = { Text("Registration Number") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                
                Text("Type (SUV, Sedan, Truck, Standard, Other)", modifier = Modifier.padding(top=8.dp), style = MaterialTheme.typography.labelSmall)
                OutlinedTextField(value = type, onValueChange = { type = it }, modifier = Modifier.fillMaxWidth())
                
                Text("Condition (Best, Good, Moderate, Poor, Not Working)", modifier = Modifier.padding(top=8.dp), style = MaterialTheme.typography.labelSmall)
                OutlinedTextField(value = condition, onValueChange = { condition = it }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = mileage, 
                    onValueChange = { mileage = it }, 
                    label = { Text("Starting Mileage (km)") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = threshold, 
                    onValueChange = { threshold = it }, 
                    label = { Text("Service Threshold Limit (km)") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (brand.isNotBlank() && model.isNotBlank()) {
                    onAdd(brand, model, type, registration, condition, mileage, threshold)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditVehicleDialog(
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onUpdate: (Vehicle) -> Unit
) {
    var brand by remember { mutableStateOf(vehicle.brand) }
    var model by remember { mutableStateOf(vehicle.model) }
    var registration by remember { mutableStateOf(vehicle.registrationNumber) }
    var type by remember { mutableStateOf(vehicle.type) }
    var condition by remember { mutableStateOf(vehicle.condition) }
    var mileage by remember { mutableStateOf(vehicle.currentMileage.toString()) }
    var threshold by remember { mutableStateOf(vehicle.serviceThreshold.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Vehicle Details") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand/Make") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = registration, onValueChange = { registration = it }, label = { Text("Registration Number") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                
                Text("Type", modifier = Modifier.padding(top=8.dp), style = MaterialTheme.typography.labelSmall)
                OutlinedTextField(value = type, onValueChange = { type = it }, modifier = Modifier.fillMaxWidth())
                
                Text("Condition", modifier = Modifier.padding(top=8.dp), style = MaterialTheme.typography.labelSmall)
                OutlinedTextField(value = condition, onValueChange = { condition = it }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = mileage, 
                    onValueChange = { mileage = it }, 
                    label = { Text("Current Mileage (km)") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = threshold, 
                    onValueChange = { threshold = it }, 
                    label = { Text("Service Threshold Limit (km)") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (brand.isNotBlank() && model.isNotBlank()) {
                    val mVal = mileage.toFloatOrNull() ?: vehicle.currentMileage
                    val tVal = threshold.toFloatOrNull() ?: vehicle.serviceThreshold
                    onUpdate(
                        vehicle.copy(
                            brand = brand,
                            model = model,
                            registrationNumber = registration,
                            type = type,
                            condition = condition,
                            currentMileage = mVal,
                            serviceThreshold = tVal
                        )
                    )
                }
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ServiceHistoryDialog(vehicle: Vehicle, viewModel: FleetViewModel, onDismiss: () -> Unit) {
    val recordsFlow = remember(vehicle.id) { viewModel.getServiceRecords(vehicle.id) }
    val records by recordsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    
    var showAddForm by remember { mutableStateOf(false) }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Service History", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { showAddForm = !showAddForm }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Record")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                if (showAddForm) {
                    OutlinedTextField(
                        value = cost,
                        onValueChange = { cost = it },
                        label = { Text("Cost") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Technician Notes") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        minLines = 3
                    )
                    Button(
                        onClick = {
                            val c = cost.toDoubleOrNull() ?: 0.0
                            if (notes.isNotBlank()) {
                                viewModel.addServiceRecord(vehicle.id, c, notes)
                                showAddForm = false
                                cost = ""
                                notes = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save Record") }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                }

                if (records.isEmpty()) {
                    Text("No service records found.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    val serviceListState = rememberLazyListState()
                    LazyColumn(
                        state = serviceListState,
                        modifier = Modifier
                            .heightIn(max = 280.dp)
                            .drawScrollbar(serviceListState)
                    ) {
                        items(records) { record ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = dateFormat.format(java.util.Date(record.date)), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Text(text = "$${String.format("%.2f", record.cost)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = record.technicianNotes, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
