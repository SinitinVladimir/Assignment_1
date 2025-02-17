# Android Network Measurements (Assignment 2)

## Overview
This project extends Assignment 1 by adding advanced network measurement capabilities. It measures key performance metrics (IAT, RTT, throughput) using both TCP and UDP protocols and retrieves detailed network info (WiFi & Mobile Data).

## Features
- **Advanced Metrics:** Computes Inter-Arrival Times (IAT), Round-Trip Times (RTT), and throughput.
- **Stress Testing:** Implements high-load tests (e.g., 1000 UDP packets every 10ms; 2000 TCP messages every 20ms).
- **Network Diagnostics:** Retrieves network data using ConnectivityManager, WifiManager, and TelephonyManager.
- **Wireshark Integration:** Capture and analyze network performance with detailed graphs.

## Installation & Usage
1. Clone the repository:
   
   ```bash
   git clone https://github.com/SinitinVladimir/Assignment_1.git
2. Switch to the assignment_02 branch:
   ```bash
   git checkout assignment_02
3. Open the Android project in Android Studio and run it on your device/emulator.
Run the Python CLI application:
   ```bash
   python cli_02.py
4. Use Wireshark to capture and review network performance graphs and logs.
Future Improvements
- Optimize TCP retransmission and UDP error correction mechanisms.
- Expand testing to additional networks (e.g., LAN, VPN).
- Enhance logging and visualization for more granular analysis.
