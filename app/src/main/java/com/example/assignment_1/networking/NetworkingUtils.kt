package com.example.assignment_1.networking

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

// TCP SERVER with Receiver-Side IAT
suspend fun startTCPServer(port: Int, onMessageReceived: (String, String) -> Unit) {
    withContext(Dispatchers.IO) {
        val serverSocket = ServerSocket(port)
        Log.d("TCPServer", "TCP Server started on port $port")
        try {
            var prevTime: Long? = null
            while (true) {
                val clientSocket = serverSocket.accept()
                val message = clientSocket.getInputStream().bufferedReader().readLine()

                val currTime = System.currentTimeMillis()
                if (prevTime != null) {
                    val iat = (currTime - prevTime) / 1000.0
                    Log.d("TCPServer", "Receiver-Side IAT (TCP): $iat seconds")
                } else {
                    Log.d("TCPServer", "First TCP message received, no IAT to calculate")
                }
                prevTime = currTime

                onMessageReceived("TCP", message ?: "NULL")
                clientSocket.getOutputStream().write("Message received\n".toByteArray())
                clientSocket.close()
            }
        } finally {
            serverSocket.close()
        }
    }
}

/**
 * Sends multiple TCP packets to (ip, port).
 * Now includes sender-side IAT measurement between consecutive packets.
 */
suspend fun sendTCPMessage(
    ip: String,
    port: Int,
    message: String,
    n: Int = 1,
    delay: Long = 1000
): String {
    return withContext(Dispatchers.IO) {
        var result = "Sent successfully"

        // We'll measure time between consecutive sends
        var prevSendTime: Long? = null

        try {
            for (i in 1..n) {
                // Create and connect a new socket for each packet
                Socket(ip, port).use { socket ->
                    val packetMessage = "$message (Packet $i)"

                    val currTime = System.currentTimeMillis()
                    if (prevSendTime != null) {
                        val iatMs = currTime - prevSendTime!!
                        Log.d("TCPClient", "Sender-Side IAT (TCP) between packet ${i-1} and $i: $iatMs ms")
                    }
                    prevSendTime = currTime

                    socket.getOutputStream().write("$packetMessage\n".toByteArray())
                    Log.d("TCPClient", "Sent: $packetMessage to $ip:$port")

                    // (Optional) read a response
                    socket.soTimeout = 2000
                    try {
                        val response = socket.getInputStream().bufferedReader().readLine()
                        if (response != null) {
                            Log.d("TCPClient", "Server response: $response")
                        }
                    } catch (e: Exception) {
                        Log.w("TCPClient", "No immediate response (TCP) for packet $i")
                    }
                }

                // Delay
                Thread.sleep(delay)
            }
        } catch (e: Exception) {
            Log.e("TCPClient", "Error sending TCP message: ${e.message}")
            result = "Error: ${e.message}"
        }

        result
    }
}

// UDP SERVER with Receiver-Side IAT
suspend fun startUDPServer(port: Int, onMessageReceived: (String, String) -> Unit) {
    withContext(Dispatchers.IO) {
        val socket = DatagramSocket(port)
        Log.d("UDPServer", "UDP Server started on port $port")
        val buffer = ByteArray(1024)
        var prevTime: Long? = null
        try {
            while (true) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)
                val message = String(packet.data, 0, packet.length)

                val currTime = System.currentTimeMillis()
                if (prevTime != null) {
                    val iat = (currTime - prevTime!!) / 1000.0
                    Log.d("UDPServer", "Receiver-Side IAT (UDP): $iat seconds")
                } else {
                    Log.d("UDPServer", "First UDP packet received, no IAT to calculate")
                }
                prevTime = currTime

                onMessageReceived("UDP", message)

                val response = "Message received".toByteArray()
                socket.send(DatagramPacket(response, response.size, packet.address, packet.port))
            }
        } finally {
            socket.close()
        }
    }
}

/**
 * Sends multiple UDP packets to (ip, port).
 * Now includes sender-side IAT measurement between consecutive packets.
 */
suspend fun sendUDPMessage(
    ip: String,
    port: Int,
    message: String,
    n: Int = 1,
    delay: Long = 1000
): String {
    return withContext(Dispatchers.IO) {
        val socket = DatagramSocket()
        var result = "Sent successfully"

        // For measuring consecutive send times
        var prevSendTime: Long? = null

        try {
            val address = InetAddress.getByName(ip)
            for (i in 1..n) {
                val packetMessage = "$message (Packet $i)"
                val data = packetMessage.toByteArray()
                val packet = DatagramPacket(data, data.size, address, port)

                val currTime = System.currentTimeMillis()
                if (prevSendTime != null) {
                    val iatMs = currTime - prevSendTime!!
                    Log.d("UDPClient", "Sender-Side IAT (UDP) between packet ${i-1} and $i: $iatMs ms")
                }
                prevSendTime = currTime

                socket.send(packet)
                Log.d("UDPClient", "Sent: $packetMessage to $ip:$port")

                // (Optional) read a response (single read, up to you)
                socket.soTimeout = 2000
                try {
                    val responsePacket = DatagramPacket(ByteArray(1024), 1024)
                    socket.receive(responsePacket)
                    val respMsg = String(responsePacket.data, 0, responsePacket.length)
                    Log.d("UDPClient", "Server response: $respMsg")
                } catch (e: Exception) {
                    Log.w("UDPClient", "No immediate response (UDP) for packet $i")
                }

                Thread.sleep(delay)
            }
        } catch (e: Exception) {
            Log.e("UDPClient", "Error sending UDP message: ${e.message}")
            result = "Error: ${e.message}"
        } finally {
            socket.close()
        }
        result
    }
}
