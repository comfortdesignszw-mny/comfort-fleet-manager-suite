package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.FleetViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.FileProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncCenterScreen(viewModel: FleetViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // Launcher for selecting a JSON file to import
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val jsonText = inputStream?.bufferedReader()?.use { it.readText() }
                    if (jsonText != null) {
                        val success = viewModel.syncImportJSON(jsonText)
                        if (success) {
                            syncMessage = "Data successfully imported and merged!"
                        } else {
                            syncMessage = "Failed to parse imported data."
                        }
                    } else {
                        syncMessage = "Could not read the file."
                    }
                } catch (e: Exception) {
                    syncMessage = "Import failed: \${e.message}"
                }
                showDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync Center") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Data Synchronization", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Keep your fleet data up-to-date across multiple devices seamlessly.", style = MaterialTheme.typography.bodyMedium)

            var expandedTab by remember { mutableStateOf(0) }

            // Method 1: File-Based Share
            Card {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { expandedTab = if (expandedTab == 0) -1 else 0 }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Option A: Share via File (No Internet)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Icon(if (expandedTab == 0) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null)
                    }
                    if (expandedTab == 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Step 1: Export Data to a JSON file.")
                        Text("Step 2: Share securely via WhatsApp, Bluetooth, or Email.")
                        Text("Step 3: Import File on the supervisor device.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                val json = viewModel.syncExportJSON()
                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                val fileName = "fleet_sync_\${timestamp}.json"
                                val file = File(context.cacheDir, fileName)
                                file.writeText(json)
                                
                                val uri = FileProvider.getUriForFile(context, "\${context.packageName}.fileprovider", file)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "Fleet Data Export")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Fleet Data"))
                            }) {
                                Text("Export Data")
                            }

                            FilledTonalButton(onClick = {
                                importLauncher.launch("application/json")
                            }) {
                                Text("Import File")
                            }
                        }
                    }
                }
            }

            // Method 2: Local Network P2P Sync
            Card {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { expandedTab = if (expandedTab == 1) -1 else 1 }
                    ) {
                        Icon(Icons.Filled.WifiTethering, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Option B: Direct Device Sync (Same Wi-Fi)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Icon(if (expandedTab == 1) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null)
                    }
                    if (expandedTab == 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Step 1: Supervisor clicks 'Receive Logs' to open channel.")
                        Text("Step 2: Dispatcher clicks 'Send Logs' & connects.")
                        Text("Step 3: Wait for 'Sync Complete'.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(onClick = {
                                syncMessage = "Waiting for connections on local Wi-Fi... (Scanning via P2P)"
                                showDialog = true
                            }) {
                                Text("Receive Logs")
                            }
                            Button(onClick = {
                                syncMessage = "Searching for nearby Supervisor devices..."
                                showDialog = true
                            }) {
                                Text("Send Logs")
                            }
                        }
                    }
                }
            }

            // Method 3: Cloud File Backup
            Card {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { expandedTab = if (expandedTab == 2) -1 else 2 }
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Option C: Cloud File Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Icon(if (expandedTab == 2) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null)
                    }
                    if (expandedTab == 2) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Step 1: Connect to Internet.")
                        Text("Step 2: Link your Cloud Account (GDrive/Dropbox).")
                        Text("Step 3: Download & merge with the Global Ledger.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            syncMessage = "Connecting to Cloud Storage APIs... Please check internet connectivity."
                            showDialog = true
                        }) {
                            Text("Sync Master Ledger")
                        }
                    }
                }
            }
            // Reporting and Exporting
            Card {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Global Logs & Reporting", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Export the global, consolidated database to a CSV spreadsheet.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        val csv = viewModel.syncExportCSV()
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val fileName = "global_logs_\${timestamp}.csv"
                        val file = File(context.cacheDir, fileName)
                        file.writeText(csv)
                        
                        val uri = FileProvider.getUriForFile(context, "\${context.packageName}.fileprovider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, "Consolidated Global Logs CSV")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share CSV Logs"))
                    }) {
                        Text("Download Global CSV")
                    }
                }
            }

        }
        
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Sync Status") },
                text = { Text(syncMessage ?: "") },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) { Text("OK") }
                }
            )
        }
    }
}
