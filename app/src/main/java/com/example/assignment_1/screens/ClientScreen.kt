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
    var ip by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf("TCP") }
    var message by remember { mutableStateOf("") }
    var log by remember { mutableStateOf("Logs:\n") }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(value = ip, onValueChange = { ip = it }, label = { Text("Enter IP") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = port, onValueChange = { port = it }, label = { Text("Enter Port") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = message, onValueChange = { message = it }, label = { Text("Enter Message") }, modifier = Modifier.fillMaxWidth())
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
                            val response = sendTCPMessage(ip, portNumber, message)
                            log += "TCP: Sent: $message | Response: $response\n"
                        } else {
                            val response = sendUDPMessage(ip, portNumber, message)
                            log += "UDP: Sent: $message | Response: $response\n"
                        }
                    } else {
                        log += "Invalid port number\n"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Send Message") }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = log, modifier = Modifier.weight(1f))
    }
}
