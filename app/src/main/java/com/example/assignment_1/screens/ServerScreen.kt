package com.example.assignment_1.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assignment_1.networking.startTCPServer
import com.example.assignment_1.networking.startUDPServer
import kotlinx.coroutines.launch

@Composable
fun ServerScreen() {
    var port by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf("TCP") }

    // Rolling log to display server messages & IAT
    var log by remember { mutableStateOf("Logs:\n") }

    // Track the last-received timestamp to compute IAT
    var lastMessageTime by remember { mutableStateOf<Long?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Port input
        TextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("Enter Port") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // TCP / UDP Protocol Switch
        Row {
            Button(
                onClick = { protocol = "TCP" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (protocol == "TCP") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("TCP")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { protocol = "UDP" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (protocol == "UDP") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("UDP")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Start Server Button
        Button(
            onClick = {
                coroutineScope.launch {
                    val portNumber = port.toIntOrNull()
                    if (portNumber == null) {
                        log += "Invalid port number\n"
                        return@launch
                    }

                    if (protocol == "TCP") {
                        log += "Starting TCP Server on port $portNumber...\n"
                        startTCPServer(portNumber) { proto, message ->
                            val currentTime = System.currentTimeMillis()
                            val iat = lastMessageTime?.let { (currentTime - it) / 1000.0 }
                            lastMessageTime = currentTime

                            log += if (iat != null) {
                                "$proto: $message | IAT: $iat seconds\n"
                            } else {
                                "$proto: $message | First message\n"
                            }
                        }
                    } else {
                        log += "Starting UDP Server on port $portNumber...\n"
                        startUDPServer(portNumber) { proto, message ->
                            val currentTime = System.currentTimeMillis()
                            val iat = lastMessageTime?.let { (currentTime - it) / 1000.0 }
                            lastMessageTime = currentTime

                            log += if (iat != null) {
                                "$proto: $message | IAT: $iat seconds\n"
                            } else {
                                "$proto: $message | First message\n"
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Server")
        }

        Spacer(Modifier.height(16.dp))

        // Log display
        Text(text = log, modifier = Modifier.weight(1f))
    }
}
