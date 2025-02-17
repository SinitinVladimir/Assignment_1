package com.example.assignment_1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.assignment_1.screens.ClientScreen
import com.example.assignment_1.screens.NetworkInfoScreen
import com.example.assignment_1.screens.ServerScreen
import com.example.assignment_1.theme.Assignment_1Theme

/**
 * MainActivity launches Compose UI and runtime permission checks
 */
class MainActivity : ComponentActivity() {

    /**
     * Permissions needed for network and telephony data
     */
    private val dangerousPermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CHANGE_WIFI_STATE
    )

    private val INTERNET_PERMISSION_CODE = 111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkInternetPermission()
        setContent {
            Assignment_1Theme {
                PermissionCheckAndContent(dangerousPermissions)
            }
        }
    }

    private fun checkInternetPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.INTERNET), INTERNET_PERMISSION_CODE)
        } else {
            Toast.makeText(this, "Internet permission already granted", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * PermissionCheckAndContent requests multiple permissions, then shows main UI
 */
@Composable
fun PermissionCheckAndContent(dangerousPerms: Array<String>) {
    val ctx = LocalContext.current

    // Launcher for multiple permissions
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { !it }) {
            Toast.makeText(
                ctx,
                "Some permissions denied. Data may be incomplete.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(ctx, "All permissions granted.", Toast.LENGTH_SHORT).show()
        }
    }

    // Determine which permissions are still needed
    val needed = remember {
        dangerousPerms.filter {
            ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    // Launch request if not granted
    LaunchedEffect(Unit) {
        if (needed.isNotEmpty()) {
            permLauncher.launch(needed.toTypedArray())
        }
    }

    // Show app content
    AppContent()
}

/**
 * AppContent Bottom nav + single container for screens without scroll
 */
@Composable
fun AppContent() {
    var selectedScreen by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedScreen = selectedScreen,
                onItemSelected = { selectedScreen = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (selectedScreen) {
                0 -> ClientScreen()
                1 -> ServerScreen()
                2 -> NetworkInfoScreen()
            }
        }
    }
}

/**
 * BottomNavigationBar items: Client, Server, Network
 */
@Composable
fun BottomNavigationBar(selectedScreen: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = (selectedScreen == 0),
            onClick = { onItemSelected(0) },
            icon = { Icon(Icons.Filled.Send, contentDescription = "Client") },
            label = { Text("Client") }
        )
        NavigationBarItem(
            selected = (selectedScreen == 1),
            onClick = { onItemSelected(1) },
            icon = { Icon(Icons.Filled.Cloud, contentDescription = "Server") },
            label = { Text("Server") }
        )
        NavigationBarItem(
            selected = (selectedScreen == 2),
            onClick = { onItemSelected(2) },
            icon = { Icon(Icons.Filled.Info, contentDescription = "Network") },
            label = { Text("Network") }
        )
    }
}
