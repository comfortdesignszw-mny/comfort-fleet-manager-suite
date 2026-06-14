package com.example.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.viewmodel.FleetViewModel
import com.example.ui.components.neonInteractedGlow

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object Inventory : Screen("inventory", "Inventory", Icons.Filled.DirectionsCar)
    object TripLogs : Screen("triplogs", "Trip Logs", Icons.Filled.MenuBook)
    object Telematics : Screen("telematics", "Telematics", Icons.Filled.Map)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object SyncCenter : Screen("sync_center", "Sync Center", Icons.Filled.Settings)
}

val items = listOf(
    Screen.Dashboard,
    Screen.Inventory,
    Screen.TripLogs,
    Screen.Telematics,
    Screen.Settings
)

@Composable
fun MainAppScreen(viewModel: FleetViewModel = viewModel()) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            androidx.compose.foundation.layout.Column {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    modifier = if (isSelected) {
                                        Modifier.neonInteractedGlow(isInteracted = true)
                                    } else {
                                        Modifier
                                    }
                                )
                            },
                            label = { Text(screen.title) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Text(
                        text = "© 2026 Comfort Fleet Manager Suite. All Rights Reserved",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.material3.Text(
                        text = "Designed with ❤️ by Comfort Designs - +263772824132",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            val defaultBackAction = {
                if (!navController.popBackStack()) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            composable(Screen.Dashboard.route) { 
                DashboardScreen(viewModel) 
            }
            composable(Screen.Inventory.route) { 
                InventoryScreen(viewModel, onBack = defaultBackAction) 
            }
            composable(Screen.TripLogs.route) { 
                TripLogsScreen(viewModel, onBack = defaultBackAction) 
            }
            composable(Screen.Telematics.route) { 
                TelematicsScreen(viewModel, onBack = defaultBackAction) 
            }
            composable(Screen.Settings.route) { 
                SettingsScreen(
                    viewModel, 
                    onBack = defaultBackAction, 
                    onNavigateToSync = { navController.navigate(Screen.SyncCenter.route) }
                ) 
            }
            composable(Screen.SyncCenter.route) {
                SyncCenterScreen(viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
