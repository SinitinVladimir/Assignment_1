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
    var log by remember { mutableStateOf("Logs:\n") }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("Enter Port") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(
                onClick = { protocol = "TCP" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (protocol == "TCP") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) { Text("TCP") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { protocol = "UDP" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (protocol == "UDP") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) { Text("UDP") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    val portNumber = port.toIntOrNull()
                    if (portNumber != null) {
                        if (protocol == "TCP") {
                            log += "Starting TCP Server on port $portNumber\n"
                            startTCPServer(portNumber) { _, message ->
                                log += "TCP: $message\n"
                            }
                        } else {
                            log += "Starting UDP Server on port $portNumber\n"
                            startUDPServer(portNumber) { _, message ->
                                log += "UDP: $message\n"
                            }
                        }
                    } else {
                        log += "Invalid port number\n"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Start Server") }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = log, modifier = Modifier.weight(1f))
    }
}
