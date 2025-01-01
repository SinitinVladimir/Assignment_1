package com.example.assignment_1.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.*
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

@Composable
fun NetworkInfoScreen() {
    val context = LocalContext.current

    // Normal permissions (no user prompt needed)
    val canAccessNetworkState = checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)
    val canAccessWifiState = checkPermission(context, Manifest.permission.ACCESS_WIFI_STATE)

    // Dangerous permissions (prompted in MainActivity)
    val canReadPhoneState = checkPermission(context, Manifest.permission.READ_PHONE_STATE)
    val canAccessFineLocation = checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    // Connectivity info
    val (activeNetType, allNetTypes) = remember {
        if (canAccessNetworkState) getConnectivityInfo(connectivityManager)
        else "Permission not granted" to emptyList<String>()
    }

    // WiFi info
    val wifiData = remember {
        if (canAccessWifiState) safeGetWifiInfo(wifiManager) else null
    }

    // Telephony info
    val telephonyData = remember {
        if (canReadPhoneState) getTelephonyInfo(telephonyManager, canAccessFineLocation)
        else TelephonyData(
            dataState = "Permission not granted",
            phoneType = "N/A",
            networkType = "N/A",
            operatorName = "N/A",
            simOperatorName = "N/A",
            cellLocation = "N/A"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Network Information", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Text("ConnectivityManager:")
        Text(" • Active Network: $activeNetType")
        Text(" • All Network Types: ${allNetTypes.joinToString()}")

        Spacer(Modifier.height(16.dp))

        Text("WiFiManager:")
        if (wifiData == null) {
            Text(" • WiFi info unavailable (permission not granted or WiFi off)")
        } else {
            Text(" • SSID: ${wifiData.ssid}")
            Text(" • BSSID: ${wifiData.bssid}")
            Text(" • Link Speed: ${wifiData.linkSpeed} Mbps")
            Text(" • RSSI (Signal Level): ${wifiData.rssi} dBm")
            val ip = wifiData.ipAddress
            Text(" • IP Address: ${formatIpAddress(ip)}")
        }

        Spacer(Modifier.height(16.dp))

        Text("TelephonyManager:")
        Text(" • Data State: ${telephonyData.dataState}")
        Text(" • Phone Type: ${telephonyData.phoneType}")
        Text(" • Network Type: ${telephonyData.networkType}")
        Text(" • Network Operator Name: ${telephonyData.operatorName}")
        Text(" • SIM Operator Name: ${telephonyData.simOperatorName}")
        Text(" • Cell Location: ${telephonyData.cellLocation}")
    }
}

@Composable
private fun checkPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

private fun getConnectivityInfo(
    cm: ConnectivityManager
): Pair<String, List<String>> {
    val activeNetwork = cm.activeNetwork
    val activeCaps = activeNetwork?.let { cm.getNetworkCapabilities(it) }
    val activeType = when {
        activeCaps == null -> "None"
        activeCaps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
        activeCaps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
        activeCaps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
        else -> "Unknown"
    }

    val allNetworks = cm.allNetworks.mapNotNull { network ->
        val caps = cm.getNetworkCapabilities(network) ?: return@mapNotNull null
        when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> null
        }
    }

    return activeType to allNetworks
}

private fun safeGetWifiInfo(wifiManager: WifiManager): WifiInfo? {
    return try {
        wifiManager.connectionInfo
    } catch (e: SecurityException) {
        Log.e("NetworkInfoScreen", "SecurityException: ${e.message}")
        null
    }
}

data class TelephonyData(
    val dataState: String,
    val phoneType: String,
    val networkType: String,
    val operatorName: String,
    val simOperatorName: String,
    val cellLocation: String
)

private fun getTelephonyInfo(
    telephonyManager: TelephonyManager,
    hasLocationPermission: Boolean
): TelephonyData {
    val dataStateStr = when (telephonyManager.dataState) {
        TelephonyManager.DATA_CONNECTED -> "CONNECTED"
        TelephonyManager.DATA_CONNECTING -> "CONNECTING"
        TelephonyManager.DATA_DISCONNECTED -> "DISCONNECTED"
        TelephonyManager.DATA_SUSPENDED -> "SUSPENDED"
        else -> "UNKNOWN"
    }

    val phoneTypeStr = when (telephonyManager.phoneType) {
        TelephonyManager.PHONE_TYPE_GSM -> "GSM"
        TelephonyManager.PHONE_TYPE_CDMA -> "CDMA"
        TelephonyManager.PHONE_TYPE_SIP -> "SIP"
        TelephonyManager.PHONE_TYPE_NONE -> "NONE"
        else -> "UNKNOWN"
    }

    val networkTypeStr = when (telephonyManager.networkType) {
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
        TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
        TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
        TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
        TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO_0"
        TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO_A"
        TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
        else -> "UNKNOWN"
    }

    val operatorName = telephonyManager.networkOperatorName ?: "N/A"
    val simOperatorName = telephonyManager.simOperatorName ?: "N/A"

    val cellLocationStr = if (hasLocationPermission) {
        try {
            telephonyManager.cellLocation?.toString() ?: "N/A"
        } catch (e: SecurityException) {
            "Permission denied"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    } else {
        "Location permission not granted"
    }

    return TelephonyData(
        dataState = dataStateStr,
        phoneType = phoneTypeStr,
        networkType = networkTypeStr,
        operatorName = operatorName,
        simOperatorName = simOperatorName,
        cellLocation = cellLocationStr
    )
}

private fun formatIpAddress(ip: Int): String {
    return String.format(Locale.US, "%d.%d.%d.%d",
        (ip and 0xff),
        (ip shr 8 and 0xff),
        (ip shr 16 and 0xff),
        (ip shr 24 and 0xff)
    )
}
