package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.CompanyProfile
import com.example.viewmodel.FleetViewModel

import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: FleetViewModel, onBack: () -> Unit) {
    val currentProfile by viewModel.companyProfile.collectAsStateWithLifecycle()
    
    var contactName by remember(currentProfile) { mutableStateOf(currentProfile.contactName) }
    var contactNumber by remember(currentProfile) { mutableStateOf(currentProfile.contactNumber) }
    var contactEmail by remember(currentProfile) { mutableStateOf(currentProfile.contactEmail) }
    var companyName by remember(currentProfile) { mutableStateOf(currentProfile.companyName) }
    var companyNumber by remember(currentProfile) { mutableStateOf(currentProfile.companyNumber) }
    var companyEmail by remember(currentProfile) { mutableStateOf(currentProfile.companyEmail) }
    var companyAddress by remember(currentProfile) { mutableStateOf(currentProfile.companyAddress) }

    var savedMessageVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Profile", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.saveCompanyProfile(
                    CompanyProfile(
                        contactName = contactName,
                        contactNumber = contactNumber,
                        contactEmail = contactEmail,
                        companyName = companyName,
                        companyNumber = companyNumber,
                        companyEmail = companyEmail,
                        companyAddress = companyAddress
                    )
                )
                savedMessageVisible = true
            }) {
                Icon(Icons.Filled.Save, contentDescription = "Save Profile")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Personal Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(value = contactName, onValueChange = { contactName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = contactNumber, onValueChange = { contactNumber = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = contactEmail, onValueChange = { contactEmail = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Company Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(value = companyName, onValueChange = { companyName = it }, label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = companyNumber, onValueChange = { companyNumber = it }, label = { Text("Company Reg / Identifying Number") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = companyEmail, onValueChange = { companyEmail = it }, label = { Text("Company Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = companyAddress, onValueChange = { companyAddress = it }, label = { Text("Company Address") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            
            if (savedMessageVisible) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Profile successfully updated!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}
