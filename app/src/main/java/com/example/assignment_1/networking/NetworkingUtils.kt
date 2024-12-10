package com.example.assignment_1.networking

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

// TCP Server
suspend fun startTCPServer(port: Int, onMessageReceived: (String, String) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            val serverSocket = ServerSocket(port)
            Log.d("TCPServer", "TCP Server started on port $port")
            try {
                while (true) {
                    val clientSocket = serverSocket.accept()
                    val clientAddress = clientSocket.inetAddress.hostAddress
                    try {
                        val reader = clientSocket.getInputStream().bufferedReader()
                        val message = reader.readLine()  // Read message
                        Log.d("TCPServer", "Message received: $message from $clientAddress")

                        onMessageReceived("TCP", "From $clientAddress: $message")

                        // Send response
                        val response = "Acknowledged: $message"
                        clientSocket.getOutputStream().write((response + "\n").toByteArray())
                    } catch (e: Exception) {
                        Log.e("TCPServer", "Error processing message: ${e.message}")
                    } finally {
                        clientSocket.close()  // Always close the socket
                    }
                }
            } finally {
                serverSocket.close()
            }
        } catch (e: Exception) {
            Log.e("TCPServer", "Error starting TCP server: ${e.message}")
            onMessageReceived("TCP", "Error: ${e.message}")
        }
    }
}


// TCP Client
suspend fun sendTCPMessage(ip: String, port: Int, message: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val socket = Socket(ip, port)
            Log.d("TCPClient", "Connected to TCP server $ip:$port")
            socket.getOutputStream().write((message + "\n").toByteArray())
            Log.d("TCPClient", "Message sent: $message")

            val response = socket.getInputStream().bufferedReader().readLine()
            Log.d("TCPClient", "Response received: $response")
            socket.close()
            response
        } catch (e: Exception) {
            Log.e("TCPClient", "Error sending TCP message: ${e.message}")
            "Error: ${e.message}"
        }
    }
}

// UDP Server
suspend fun startUDPServer(port: Int, onMessageReceived: (String, String) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            val socket = DatagramSocket(port)
            Log.d("UDPServer", "UDP Server started on port $port")
            val buffer = ByteArray(1024)
            try {
                while (true) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet) // Receive packet from client

                    // Extract message details
                    val message = String(packet.data, 0, packet.length)
                    val clientAddress = packet.address.hostAddress
                    val clientPort = packet.port

                    // Log received message
                    Log.d("UDPServer", "Received message from $clientAddress:$clientPort: $message")
                    onMessageReceived("UDP", "From $clientAddress:$clientPort: $message")

                    // Prepare and send response
                    val responseMessage = "Acknowledged: $message"
                    val response = responseMessage.toByteArray()
                    val responsePacket = DatagramPacket(response, response.size, packet.address, packet.port)
                    socket.send(responsePacket)

                    // Log response details
                    Log.d("UDPServer", "Response sent to $clientAddress:$clientPort: $responseMessage")
                }
            } catch (e: Exception) {
                Log.e("UDPServer", "Error during packet processing: ${e.message}")
            } finally {
                socket.close()
                Log.d("UDPServer", "UDP Server socket closed.")
            }
        } catch (e: Exception) {
            Log.e("UDPServer", "Error starting UDP server: ${e.message}")
            onMessageReceived("UDP", "Error: ${e.message}")
        }
    }
}


// UDP Client
suspend fun sendUDPMessage(ip: String, port: Int, message: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val socket = DatagramSocket()
            val address = InetAddress.getByName(ip)
            val packet = DatagramPacket(message.toByteArray(), message.length, address, port)
            socket.send(packet)
            Log.d("UDPClient", "Message sent to $ip:$port: $message")

            val buffer = ByteArray(1024)
            val responsePacket = DatagramPacket(buffer, buffer.size)
            socket.receive(responsePacket)
            val response = String(responsePacket.data, 0, responsePacket.length)
            Log.d("UDPClient", "Response received from $ip:$port: $response")
            socket.close()
            response
        } catch (e: Exception) {
            Log.e("UDPClient", "Error sending UDP message: ${e.message}")
            "Error: ${e.message}"
        }
    }
}
