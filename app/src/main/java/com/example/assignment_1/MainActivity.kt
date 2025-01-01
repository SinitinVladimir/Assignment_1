package com.example.assignment_1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.assignment_1.screens.ClientScreen
import com.example.assignment_1.screens.NetworkInfoScreen
import com.example.assignment_1.screens.ServerScreen
import com.example.assignment_1.ui.theme.Assignment_1Theme
import androidx.compose.ui.platform.LocalContext


class MainActivity : ComponentActivity() {

    /**
     * We’ll request these two “dangerous” permissions at runtime:
     * - READ_PHONE_STATE (for telephony info)
     * - ACCESS_FINE_LOCATION (for cell location, Wi-Fi SSID/BSSID, etc.)
     *
     * Also note: “INTERNET” is declared in the manifest, but it usually
     * does NOT need a runtime request. Same for “ACCESS_NETWORK_STATE”
     * and “ACCESS_WIFI_STATE” (they’re normal permissions).
     */
    private val dangerousPermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Check if Internet permission is declared (usually no runtime prompt needed)
        if (!isInternetPermissionGranted()) {
            requestInternetPermission()
        } else {
            Toast.makeText(this, "Internet permission already granted", Toast.LENGTH_SHORT).show()
        }

        setContent {
            Assignment_1Theme {
                // 2) Inside Compose, we’ll do a runtime permission check for dangerous perms
                PermissionsAndContent()
            }
        }
    }

    /**
     * A simple check for INTERNET permission – typically not dangerous,
     * but you had this in your original code. Usually not required at runtime.
     */
    private fun isInternetPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestInternetPermission() {
        // For demonstration. Usually “INTERNET” is granted automatically if in manifest.
        requestPermissions(arrayOf(Manifest.permission.INTERNET), INTERNET_PERMISSION_REQUEST_CODE)
    }

    companion object {
        private const val INTERNET_PERMISSION_REQUEST_CODE = 1
    }
}

/**
 * This composable is launched in setContent().
 * It checks the “dangerous” permissions using the ActivityResult API.
 */
@Composable
fun PermissionsAndContent() {
    val context = LocalContext.current

    // 1) We'll use rememberLauncherForActivityResult to request multiple perms at once.
    //    This avoids the deprecated onRequestPermissionsResult approach.
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        // This callback is invoked with a map of permission -> granted/denied.
        // e.g. {READ_PHONE_STATE=true, ACCESS_FINE_LOCATION=false} if user allows phone but denies location
        if (permissionsMap.values.any { !it }) {
            // If any permission was denied, show a Toast or handle accordingly
            Toast.makeText(
                context,
                "Some permissions were denied; network info may be incomplete.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            // All dangerous permissions granted
            Toast.makeText(
                context,
                "All permissions granted! Full network info available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 2) Check if they’re already granted
    val readPhoneStateGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED

    val fineLocationGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // 3) If not all are granted, launch the permission request. You could also do this on a button click.
    LaunchedEffect(Unit) {
        if (!readPhoneStateGranted || !fineLocationGranted) {
            multiplePermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    // 4) Show the actual UI – a scaffold with bottom nav for Client/Server/Network screens
    AppContent()
}

/**
 * Your existing composable, updated to have 3 screens:
 * - 0 -> Client
 * - 1 -> Server
 * - 2 -> Network
 */
@Composable
fun AppContent() {
    var selectedScreen by remember { mutableIntStateOf(0) }  // If using Compose 1.5+, or mutableStateOf(0) otherwise

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedScreen = selectedScreen,
                onItemSelected = { selectedScreen = it }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedScreen) {
                0 -> ClientScreen()
                1 -> ServerScreen()
                2 -> NetworkInfoScreen()  // The new screen
            }
        }
    }
}

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
