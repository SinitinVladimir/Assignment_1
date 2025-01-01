package com.example.assignment_1.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assignment_1.networking.sendTCPMessage
import com.example.assignment_1.networking.sendUDPMessage
import kotlinx.coroutines.launch

@Composable
fun ClientScreen() {
    // User inputs
    var ip by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf("TCP") }
    var message by remember { mutableStateOf("") }
    var packets by remember { mutableStateOf("1") }
    var delayMs by remember { mutableStateOf("1000") }

    // Log text for the UI
    var log by remember { mutableStateOf("Logs:\n") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // IP
        TextField(
            value = ip,
            onValueChange = { ip = it },
            label = { Text("Enter IP Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Port
        TextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("Enter Port") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Message
        TextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Enter Message") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Number of Packets
        TextField(
            value = packets,
            onValueChange = { packets = it },
            label = { Text("Number of Packets") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Delay
        TextField(
            value = delayMs,
            onValueChange = { delayMs = it },
            label = { Text("Delay (ms) Between Packets") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Protocol Buttons
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

        // Send Button
        Button(
            onClick = {
                coroutineScope.launch {
                    val portNumber = port.toIntOrNull()
                    if (portNumber == null) {
                        log += "Invalid port number\n"
                        return@launch
                    }
                    val numPackets = packets.toIntOrNull() ?: 1
                    val delayMillis = delayMs.toLongOrNull() ?: 1000

                    log += "Sending $numPackets packet(s) via $protocol...\n"
                    val sendResult = if (protocol == "TCP") {
                        sendTCPMessage(ip, portNumber, message, numPackets, delayMillis)
                    } else {
                        sendUDPMessage(ip, portNumber, message, numPackets, delayMillis)
                    }
                    log += "$sendResult\n"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Message")
        }

        Spacer(Modifier.height(16.dp))

        // Log display area
        Text(text = log, modifier = Modifier.weight(1f))
    }
}
