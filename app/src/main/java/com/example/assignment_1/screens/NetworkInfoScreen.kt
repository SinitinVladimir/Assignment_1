package com.example.assignment_1.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.*
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.*
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Locale

@Composable
fun NetworkInfoScreen() {
    val context = LocalContext.current

    // Required permissions
    val canAccessNetworkState = checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)
    val canAccessWifiState = checkPermission(context, Manifest.permission.ACCESS_WIFI_STATE)
    val canChangeWifiState = checkPermission(context, Manifest.permission.CHANGE_WIFI_STATE)
    val canReadPhoneState = checkPermission(context, Manifest.permission.READ_PHONE_STATE)
    val canFineLoc = checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    val canCoarseLoc = checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

    // Managers
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    // ------------- (1) Connectivity Information -------------
    val (activeNetType, allNetTypes) = remember {
        if (canAccessNetworkState) {
            getConnectivityInfo(connectivityManager)
        } else {
            "Permission not granted" to emptyList<String>()
        }
    }

    // ------------- 2 Wi-Fi Network -------------
    // 2.1 ConnectionInfo (deprecated but assignment requires)
    val wifiInfo = remember {
        if (canAccessWifiState) safeGetWifiInfo(wifiManager) else null
    }

    // 2.2 DhcpInfo (deprecated, no direct replacement)
    val dhcp = remember {
        if (canAccessWifiState) {
            try {
                @Suppress("DEPRECATION")
                wifiManager.dhcpInfo // for assignment
            } catch (e: SecurityException) {
                Log.e("NetworkInfoScreen", "SecurityException: ${e.message}")
                null
            }
        } else null
    }

    // 2.3 ConfiguredNetworks
    val wifiConfiguredNetworks = remember {
        if (canAccessWifiState || canChangeWifiState) {
            try {
                @Suppress("DEPRECATION")
                wifiManager.configuredNetworks
            } catch (e: SecurityException) {
                Log.e("NetworkInfoScreen", "SecurityException: ${e.message}")
                null
            }
        } else null
    }

    // ------------- 3 Mobile Network -------------
    // Initialize with default data
    var telephonyData by remember {
        mutableStateOf(
            MobileNetworkData(
                dataState = "Loading...",
                phoneType = "Loading...",
                networkType = "Loading...",
                cellId = "Loading...",
                lac = "Loading...",
                mcc = "Loading...",
                mnc = "Loading...",
                networkOperatorName = "Loading...",
                simOperatorName = "Loading...",
                latLong = "Loading..."
            )
        )
    }

    // Fetch mobile network info asynchronously
    LaunchedEffect(key1 = Unit) {
        if (canReadPhoneState) {
            telephonyData = getMobileNetworkInfo(
                telephonyManager,
                canFineLoc,
                canCoarseLoc
            )
        } else {
            telephonyData = MobileNetworkData(
                dataState = "Permission not granted",
                phoneType = "N/A",
                networkType = "N/A",
                cellId = "N/A",
                lac = "N/A",
                mcc = "N/A",
                mnc = "N/A",
                networkOperatorName = "N/A",
                simOperatorName = "N/A",
                latLong = "N/A"
            )
        }
    }

    // Use LazyColumn to make the entire content scrollable
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Network Information", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
        }

        item {
            Text("1. Connectivity Information")
            Text(" • Type of Active Network: $activeNetType")
            Text(" • All Other Networks: ${allNetTypes.joinToString()}")
            Spacer(Modifier.height(16.dp))
        }

        item {
            // ===== 2 WiFi =====
            Text("2. Information of a WiFi Network")

            // 2.1 ConnectionInfo
            Text("[ConnectionInfo]", style = MaterialTheme.typography.titleSmall)
            if (wifiInfo == null) {
                Text("   WiFi info unavailable (permission not granted or WiFi off)")
            } else {
                Text("   IP Address: ${formatIpAddress(wifiInfo.ipAddress)}")
                Text("   MAC Address: ${wifiInfo.macAddress ?: "N/A"}")
                Text("   Link Speed: ${wifiInfo.linkSpeed} Mbps")
                Text("   SSID: ${wifiInfo.ssid}")
                Text("   BSSID: ${wifiInfo.bssid}")
                Text("   RSSI: ${wifiInfo.rssi} dBm")
            }

            // 2.2 DhcpInfo
            Text("[DhcpInfo]", style = MaterialTheme.typography.titleSmall)
            if (dhcp != null) {
                Text("   IP: ${formatIpAddress(dhcp.ipAddress)}")
                Text("   Gateway: ${formatIpAddress(dhcp.gateway)}")
                Text("   Netmask: ${formatIpAddress(dhcp.netmask)}")
                Text("   DNS1: ${formatIpAddress(dhcp.dns1)}")
                Text("   DNS2: ${formatIpAddress(dhcp.dns2)}")
                Text("   Server Address: ${formatIpAddress(dhcp.serverAddress)}")
            } else {
                Text("   DhcpInfo not available or permission missing.")
            }

            // 2.3 ConfiguredNetworks
            Text("[ConfiguredNetworks]", style = MaterialTheme.typography.titleSmall)
            if (wifiConfiguredNetworks == null) {
                Text("   Configured networks unavailable (permission missing?).")
            } else if (wifiConfiguredNetworks.isEmpty()) {
                Text("   No configured networks.")
            } else {
                wifiConfiguredNetworks.forEach { cfg ->
                    // WifiConfiguration is deprecated, but it has fields: networkId, SSID, BSSID, priority
                    Text("   Network ID: ${cfg.networkId}")
                    Text("   SSID: ${cfg.SSID}")
                    Text("   BSSID: ${cfg.BSSID ?: "N/A"}")
                    Text("   Priority: ${cfg.priority}")
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        item {
            // ===== 3 Mobile Network =====
            Text("3. Information of a Mobile Network")
            Text(" • DataState: ${telephonyData.dataState}")
            Text(" • PhoneType: ${telephonyData.phoneType}")
            Text(" • NetworkType: ${telephonyData.networkType}")
            Text(" • CellID: ${telephonyData.cellId}")
            Text(" • LAC: ${telephonyData.lac}")
            Text(" • MCC: ${telephonyData.mcc}")
            Text(" • MNC: ${telephonyData.mnc}")
            Text(" • NetworkOperatorName: ${telephonyData.networkOperatorName}")
            Text(" • SimOperatorName: ${telephonyData.simOperatorName}")
            Text(" • Lat/Long from cell: ${telephonyData.latLong}")
        }
    }
}

// ---------------------- Helpers ----------------------

@Composable
private fun checkPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

// ============ 1. Connectivity Info =============
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

    val allNets = cm.allNetworks.mapNotNull { net ->
        if (net == activeNetwork) return@mapNotNull null
        val caps = cm.getNetworkCapabilities(net) ?: return@mapNotNull null
        when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> null
        }
    }

    return activeType to allNets
}

// ============ 2 WiFi Info =============

@Suppress("DEPRECATION")
private fun safeGetWifiInfo(wifiManager: WifiManager): WifiInfo? {
    return try {
        wifiManager.connectionInfo
    } catch (e: SecurityException) {
        Log.e("NetworkInfoScreen", "SecurityException: ${e.message}")
        null
    } catch (e: Exception) {
        Log.e("NetworkInfoScreen", "Error reading wifi info: ${e.message}")
        null
    }
}

private fun formatIpAddress(ip: Int): String {
    if (ip == 0) return "0.0.0.0"
    return String.format(
        Locale.US,
        "%d.%d.%d.%d",
        ip and 0xff,
        ip shr 8 and 0xff,
        ip shr 16 and 0xff,
        ip shr 24 and 0xff
    )
}

/// ============ 3 Mobile Network =============
data class MobileNetworkData(
    val dataState: String,
    val phoneType: String,
    val networkType: String,
    val cellId: String,
    val lac: String,
    val mcc: String,
    val mnc: String,
    val networkOperatorName: String,
    val simOperatorName: String,
    val latLong: String
)

@SuppressLint("MissingPermission")
private suspend fun getMobileNetworkInfo(
    tm: TelephonyManager,
    hasFineLocation: Boolean,
    hasCoarseLocation: Boolean
): MobileNetworkData {
    // 1) DataState
    val ds = when (tm.dataState) {
        TelephonyManager.DATA_CONNECTED -> "CONNECTED"
        TelephonyManager.DATA_CONNECTING -> "CONNECTING"
        TelephonyManager.DATA_DISCONNECTED -> "DISCONNECTED"
        TelephonyManager.DATA_SUSPENDED -> "SUSPENDED"
        else -> "UNKNOWN"
    }

    // 2) PhoneType
    val pt = when (tm.phoneType) {
        TelephonyManager.PHONE_TYPE_GSM -> "GSM"
        TelephonyManager.PHONE_TYPE_CDMA -> "CDMA"
        TelephonyManager.PHONE_TYPE_SIP -> "SIP"
        TelephonyManager.PHONE_TYPE_NONE -> "NONE"
        else -> "UNKNOWN"
    }

    // 3) networkType => dataNetworkType if available, else fallback to networkType
    val netTypeCode = try {
        tm.dataNetworkType
    } catch (e: Exception) {
        tm.networkType // fallback
    }

    val netType = when (netTypeCode) {
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

    // 4) Operator info
    val netOpName = tm.networkOperatorName ?: "N/A"
    val simOpName = tm.simOperatorName ?: "N/A"

    // 5) allCellInfo
    var cidStr = "N/A"
    var lacStr = "N/A"
    var mccStr = "N/A"
    var mncStr = "N/A"

    if (hasFineLocation || hasCoarseLocation) {
        val allCells = try {
            tm.allCellInfo
        } catch (e: SecurityException) {
            emptyList<CellInfo>()
        } catch (e: Exception) {
            emptyList<CellInfo>()
        }

        val gsmCell = allCells?.find { it is CellInfoGsm } as? CellInfoGsm
        if (gsmCell != null) {
            val gsmId = gsmCell.cellIdentity
            // Compare with -1 to see if unknown
            val c = gsmId.cid
            if (c != -1) {
                cidStr = c.toString()
            }
            val l = gsmId.lac
            if (l != -1) {
                lacStr = l.toString()
            }

            // Fallback to telephonyManager.networkOperator if missing
            val netOp = tm.networkOperator ?: ""
            if (netOp.length >= 3) {
                mccStr = netOp.substring(0, 3)
                mncStr = if (netOp.length > 3) netOp.substring(3) else "N/A"
            }
        } else {
            // fallback if no GSM cell found
            val netOp = tm.networkOperator ?: ""
            if (netOp.length >= 3) {
                mccStr = netOp.substring(0, 3)
                mncStr = if (netOp.length > 3) netOp.substring(3) else "N/A"
            }
        }
    }

    val latLongStr = convertCellIdToLatLong(mccStr, mncStr, cidStr, lacStr)

    return MobileNetworkData(
        dataState = ds,
        phoneType = pt,
        networkType = netType,
        cellId = cidStr,
        lac = lacStr,
        mcc = mccStr,
        mnc = mncStr,
        networkOperatorName = netOpName,
        simOperatorName = simOpName,
        latLong = latLongStr
    )
}

/**
 * cellphonetrackers.org with no API key needed.
 */
private suspend fun convertCellIdToLatLong(
    mcc: String,
    mnc: String,
    cid: String,
    lac: String
): String {
    if (mcc == "N/A" || mnc == "N/A" || cid == "N/A" || lac == "N/A") {
        return "N/A"
    }
    val url = "https://cellphonetrackers.org/gsm/gsm-tracker.php?mcc=$mcc&mnc=$mnc&lac=$lac&cid=$cid"
    val client = OkHttpClient()
    return withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext "N/A"
            val body = response.body?.string() ?: return@withContext "N/A"

            val latRegex = """Latitude:</b>\s*([\d\.\-]+)""".toRegex()
            val lonRegex = """Longitude:</b>\s*([\d\.\-]+)""".toRegex()
            val lat = latRegex.find(body)?.groupValues?.getOrNull(1)
            val lon = lonRegex.find(body)?.groupValues?.getOrNull(1)

            if (!lat.isNullOrEmpty() && !lon.isNullOrEmpty()) {
                "$lat, $lon"
            } else {
                "N/A"
            }
        } catch (io: IOException) {
            Log.e("convertCellIdToLatLong", "IOException: ${io.message}")
            "N/A"
        } catch (e: Exception) {
            Log.e("convertCellIdToLatLong", "Error: ${e.message}")
            "N/A"
        }
    }
}
