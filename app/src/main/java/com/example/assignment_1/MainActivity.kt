package com.example.assignment_1

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.assignment_1.screens.ClientScreen
import com.example.assignment_1.screens.ServerScreen
import com.example.assignment_1.ui.theme.Assignment_1Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check Internet permission
        if (!isInternetPermissionGranted()) {
            requestInternetPermission()
        } else {
            Toast.makeText(this, "Internet permission already granted", Toast.LENGTH_SHORT).show()
        }

        setContent {
            Assignment_1Theme {
                AppContent()
            }
        }
    }

    // Check if the Internet permission is granted
    private fun isInternetPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request Internet permission
    private fun requestInternetPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.INTERNET),
            INTERNET_PERMISSION_REQUEST_CODE
        )
    }

    companion object {
        private const val INTERNET_PERMISSION_REQUEST_CODE = 1
    }
}

@Composable
fun AppContent() {
    var selectedScreen by remember { mutableStateOf(0) } // Default to Client screen

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
                0 -> ClientScreen() // Client screen
                1 -> ServerScreen() // Server screen
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedScreen: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedScreen == 0,
            onClick = { onItemSelected(0) },
            icon = { Icon(Icons.Filled.Send, contentDescription = "Client") },
            label = { Text("Client") }
        )
        NavigationBarItem(
            selected = selectedScreen == 1,
            onClick = { onItemSelected(1) },
            icon = { Icon(Icons.Filled.Cloud, contentDescription = "Server") },
            label = { Text("Server") }
        )
    }
}
