package com.example.motoscanadv

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin
import kotlin.math.cos

// ==========================================
// 1. DATA MODELS & TELEMETRY DEFINITIONS
// ==========================================

data class PID(
    val id: String,
    val name: String,
    val shortName: String,
    val obdPid: String,
    var value: Float,
    val unit: String,
    val category: String,
    var min: Float,
    var max: Float,
    var warnMin: Float? = null,
    var warnMax: Float? = null,
    var colorOptimal: String? = null,
    var colorLow: String? = null,
    var colorHigh: String? = null
)

data class DTC(
    val code: String,
    val description: String,
    var status: String, // "ERROR" or "PENDING"
    val category: String
)

data class ReadinessItem(
    val id: String,
    val name: String,
    val nameIndo: String,
    var status: String // "READY", "NOT_READY"
)

data class LogFile(
    val name: String,
    val date: String,
    val size: String,
    var isFavorite: Boolean,
    val content: String
)

data class OBD2Settings(
    var connectionType: String = "Bluetooth",
    var pairedDevice: String = "ELM327 v2.1",
    var autoConnect: Boolean = true,
    var updateRate: Int = 10, // Hz
    var units: String = "Metric", // "Metric" or "Imperial"
    var language: String = "English", // "English" or "Indonesian"
    var profileName: String = "ADV 150 (BoreUp 180cc)"
)

// ==========================================
// 2. CORE ENGINE & VIEWMODEL STATE MANAGER
// ==========================================

class OBDViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs: SharedPreferences = application.getSharedPreferences("motoscan_prefs", Context.MODE_PRIVATE)

    var settings by mutableStateOf(OBD2Settings())
        private set

    val pids = mutableStateListOf<PID>()
    val dtcs = mutableStateListOf<DTC>()
    val readiness = mutableStateListOf<ReadinessItem>()
    val logs = mutableStateListOf<LogFile>()
    val graphData = mutableStateListOf<Map<String, Float>>()

    var isConnecting by mutableStateOf(false)
    var isConnected by mutableStateOf(false) // Starts disconnected, waiting for BT connection
    var isRecording by mutableStateOf(false)
    val currentLogLines = mutableStateListOf<String>()

    // Bluetooth-specific states
    var bluetoothErrorMessage by mutableStateOf<String?>(null)
    private var bluetoothSocket: BluetoothSocket? = null
    private var isRunningBluetoothLoop = false
    private var bluetoothPollingJob: kotlinx.coroutines.Job? = null
    private var simulationJob: kotlinx.coroutines.Job? = null

    // Simulator states
    var isSimulationActive by mutableStateOf(false)
    var simThrottle by mutableStateOf(45f)
    var simOverheat by mutableStateOf(false)
    var simMisfire by mutableStateOf(false)
    var simBatteryDrop by mutableStateOf(false)
    var simDriveMode by mutableStateOf("lap") // "lap", "manual", "idle"
    var activeSensorIds by mutableStateOf<Set<String>>(emptySet())

    private var simTime = 0f

    init {
        // Load settings safely from SharedPreferences
        var savedType = "Bluetooth"
        var savedDeviceName = "ELM327 v2.1"
        var savedDeviceAddress = ""
        var savedProfile = "ADV 150 (BoreUp 180cc)"
        var savedRate = 10
        var autoConnectVal = true

        try {
            savedType = prefs.getString("settings_connectionType", "Bluetooth") ?: "Bluetooth"
            savedDeviceName = prefs.getString("settings_pairedDevice", "ELM327 v2.1") ?: "ELM327 v2.1"
            savedDeviceAddress = prefs.getString("settings_pairedDeviceAddress", "") ?: ""
            savedProfile = prefs.getString("settings_profile", "ADV 150 (BoreUp 180cc)") ?: "ADV 150 (BoreUp 180cc)"
            savedRate = prefs.getInt("settings_updateRate", 10)
            autoConnectVal = prefs.getBoolean("settings_autoConnect", true)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                prefs.edit().clear().apply()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        settings = OBD2Settings(
            connectionType = savedType,
            pairedDevice = savedDeviceName,
            autoConnect = autoConnectVal,
            updateRate = savedRate,
            profileName = savedProfile
        )

        // Load initial PIDs with SharedPreferences overrides
        val defaultPids = listOf(
            PID("rpm", "RPM", "Engine Speed", "010C", 0f, "rpm", "ENGINE", 0f, 12000f, 1000f, 9500f),
            PID("coolant", "Coolant Temp", "Engine Coolant Temp", "0105", 0f, "°C", "ENGINE", -40f, 150f, null, 115f),
            PID("speed", "Vehicle Speed", "Vehicle Speed", "010D", 0f, "km/h", "ENGINE", 0f, 200f, null, 150f),
            PID("battery", "Battery Voltage", "Battery Voltage", "ATRV", 0f, "V", "OTHER", 9f, 16f, 11.5f, 15f),
            PID("throttle", "Throttle Position", "Throttle Position", "0111", 0f, "%", "SENSOR", 0f, 100f),
            PID("load", "Engine Load", "Calculated Load Value", "0104", 0f, "%", "ENGINE", 0f, 100f),
            PID("iat", "Intake Air Temp", "Intake Air Temperature", "010F", 0f, "°C", "SENSOR", -40f, 100f),
            PID("map", "MAP", "Manifold Absolute Pressure", "010B", 0f, "kPa", "SENSOR", 0f, 255f)
        )

        val loadedPids = defaultPids.map { p ->
            var pMin = p.min
            var pMax = p.max
            var pWarnMin = p.warnMin
            var pWarnMax = p.warnMax
            var pColorOptimal = p.colorOptimal ?: "#00E5FF"
            var pColorLow = p.colorLow ?: "#FFD600"
            var pColorHigh = p.colorHigh ?: "#FF1744"

            try {
                pMin = prefs.getFloat("${p.id}_min", p.min)
                pMax = prefs.getFloat("${p.id}_max", p.max)
                pWarnMin = if (prefs.contains("${p.id}_warnMin")) prefs.getFloat("${p.id}_warnMin", 0f) else p.warnMin
                pWarnMax = if (prefs.contains("${p.id}_warnMax")) prefs.getFloat("${p.id}_warnMax", 0f) else p.warnMax
                pColorOptimal = prefs.getString("${p.id}_colorOptimal", p.colorOptimal ?: "#00E5FF") ?: "#00E5FF"
                pColorLow = prefs.getString("${p.id}_colorLow", p.colorLow ?: "#FFD600") ?: "#FFD600"
                pColorHigh = prefs.getString("${p.id}_colorHigh", p.colorHigh ?: "#FF1744") ?: "#FF1744"
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Ensure min < max to prevent coerceIn IllegalArgumentException crashes
            if (pMin >= pMax) {
                pMin = p.min
                pMax = p.max
            }

            p.copy(
                min = pMin,
                max = pMax,
                warnMin = pWarnMin,
                warnMax = pWarnMax,
                colorOptimal = pColorOptimal,
                colorLow = pColorLow,
                colorHigh = pColorHigh,
                value = p.value.coerceSafely(pMin, pMax)
            )
        }
        
        pids.addAll(loadedPids)

        // Initial DTC Fault codes
        dtcs.addAll(listOf(
            DTC("P0171", "System Too Lean (Bank 1)", "ERROR", "Fuel and Air Metering"),
            DTC("P0302", "Cylinder 2 Misfire Detected", "ERROR", "Ignition System or Misfire"),
            DTC("P0420", "Catalyst System Efficiency Below Threshold (Bank 1)", "ERROR", "Auxiliary Emission Controls")
        ))

        // Initial emission readiness monitors
        readiness.addAll(listOf(
            ReadinessItem("misfire", "Misfire", "Misfire", "READY"),
            ReadinessItem("fuelSystem", "Fuel System", "Fuel System", "READY"),
            ReadinessItem("components", "Components", "Components", "READY"),
            ReadinessItem("catalyst", "Catalyst", "Catalyst", "READY"),
            ReadinessItem("heatedCatalyst", "Heated Catalyst", "Heated Catalyst", "NOT_READY"),
            ReadinessItem("evapSystem", "Evap System", "Evap System", "READY"),
            ReadinessItem("secondaryAir", "Secondary Air System", "Secondary Air System", "NOT_READY"),
            ReadinessItem("acRefrigerant", "A/C Refrigerant", "A/C Refrigerant", "READY"),
            ReadinessItem("o2Sensor", "O2 Sensor", "O2 Sensor", "READY"),
            ReadinessItem("o2Heater", "O2 Heater", "O2 Heater", "READY"),
            ReadinessItem("egrSystem", "EGR System", "EGR System", "READY")
        ))

        // Mock saved log files
        logs.addAll(listOf(
            LogFile("Log_2024-05-26_19-45-12.csv", "26/05/2024 19:45", "1.2 MB", false, ""),
            LogFile("Log_2024-05-26_18-12-08.csv", "26/05/2024 18:12", "850 KB", true, ""),
            LogFile("Log_2024-05-26_17-01-33.csv", "26/05/2024 17:01", "740 KB", false, "")
        ))

        // Seed 30 points of empty historical graph data
        for (i in 0 until 30) {
            graphData.add(mapOf(
                "rpm" to 0f,
                "coolant" to 0f,
                "speed" to 0f,
                "throttle" to 0f
            ))
        }

        // Apply connection mode selection
        isSimulationActive = false
        isConnected = false
        if (settings.autoConnect && savedDeviceAddress.isNotEmpty()) {
            connectBluetooth(savedDeviceAddress)
        }
    }

    fun toggleSimulationMode() {
        if (isSimulationActive) {
            isSimulationActive = false
            simulationJob?.cancel()
            simulationJob = null
            // Reset PIDs to 0
            pids.forEachIndexed { i, p ->
                pids[i] = p.copy(value = 0f)
            }
        } else {
            disconnectBluetooth()
            isSimulationActive = true
            startSimulationLoop()
        }
    }

    fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            var t = 0f
            while (isSimulationActive) {
                t += 0.1f
                val rpmVal = 1600f + (sin(t * 0.8f) * 0.5f + 0.5f) * 6400f
                val speedVal = (sin(t * 0.4f) * 0.5f + 0.5f) * 98f
                val throttleVal = (sin(t * 0.8f) * 0.5f + 0.5f) * 78f
                val coolantVal = 88f + sin(t * 0.1f) * 4f
                val batteryVal = 14.1f + sin(t * 0.3f) * 0.2f
                val iatVal = 34f + sin(t * 0.05f) * 2f
                val loadVal = 20f + throttleVal * 0.8f
                val mapVal = 35f + throttleVal * 0.6f

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    updatePIDValue("rpm", rpmVal)
                    updatePIDValue("speed", speedVal)
                    updatePIDValue("throttle", throttleVal)
                    updatePIDValue("coolant", coolantVal)
                    updatePIDValue("battery", batteryVal)
                    updatePIDValue("iat", iatVal)
                    updatePIDValue("load", loadVal)
                    updatePIDValue("map", mapVal)

                    graphData.add(mapOf(
                        "rpm" to rpmVal,
                        "coolant" to coolantVal,
                        "speed" to speedVal,
                        "throttle" to throttleVal
                    ))
                    if (graphData.size > 50) graphData.removeAt(0)

                    if (isRecording) {
                        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
                        currentLogLines.add("$timestamp,$rpmVal,$speedVal,$coolantVal,$batteryVal,$throttleVal,$loadVal,$mapVal")
                    }
                }
                delay(100)
            }
        }
    }

    // Method to update and persist customized configurations for any PID
    fun updatePIDConfig(
        id: String,
        min: Float,
        max: Float,
        warnMin: Float?,
        warnMax: Float?,
        colorOptimal: String?,
        colorLow: String?,
        colorHigh: String?
    ) {
        val index = pids.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = pids[index]
            pids[index] = old.copy(
                min = min,
                max = max,
                warnMin = warnMin,
                warnMax = warnMax,
                colorOptimal = colorOptimal,
                colorLow = colorLow,
                colorHigh = colorHigh,
                value = old.value.coerceSafely(min, max)
            )

            // Persist to SharedPreferences
            prefs.edit().apply {
                putFloat("${id}_min", min)
                putFloat("${id}_max", max)
                if (warnMin != null) putFloat("${id}_warnMin", warnMin) else remove("${id}_warnMin")
                if (warnMax != null) putFloat("${id}_warnMax", warnMax) else remove("${id}_warnMax")
                putString("${id}_colorOptimal", colorOptimal)
                putString("${id}_colorLow", colorLow)
                putString("${id}_colorHigh", colorHigh)
                apply()
            }
        }
    }

    fun updatePIDValue(id: String, value: Float) {
        val index = pids.indexOfFirst { it.id == id }
        if (index != -1) {
            val p = pids[index]
            pids[index] = p.copy(value = value.coerceSafely(p.min, p.max))
        }
    }

    // ------------------------------------------
    // REAL BLUETOOTH OBD2 IMPLEMENTATION (ELM327)
    // ------------------------------------------

    fun getPairedDevices(): List<Map<String, String>> {
        return try {
            val bluetoothManager = getApplication<Application>().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter
            if (adapter != null && adapter.isEnabled) {
                adapter.bondedDevices.map { device ->
                    mapOf(
                        "name" to (device.name ?: "Unknown OBD Adapter"),
                        "address" to device.address
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: SecurityException) {
            bluetoothErrorMessage = "Bluetooth permission is required to scan paired devices."
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun connectBluetooth(deviceAddress: String) {
        viewModelScope.launch {
            if (isConnecting) return@launch
            isConnecting = true
            isConnected = false
            isSimulationActive = false
            bluetoothErrorMessage = null

            try {
                val bluetoothManager = getApplication<Application>().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val adapter = bluetoothManager.adapter
                if (adapter == null) {
                    bluetoothErrorMessage = "Bluetooth is not supported on this device."
                    isConnected = false
                    isConnecting = false
                    return@launch
                }
                if (!adapter.isEnabled) {
                    bluetoothErrorMessage = "Bluetooth is turned off. Please enable Bluetooth."
                    isConnected = false
                    isConnecting = false
                    return@launch
                }

                // Offload blocking socket connection and commands to IO thread
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val device = adapter.getRemoteDevice(deviceAddress)
                    val sppUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID

                    // Try standard SPP UUID first; if that fails, use reflection on channel 1 (standard Torque Pro fallback for ELM327 clones)
                    val socket = try {
                        val s = device.createRfcommSocketToServiceRecord(sppUuid)
                        s.connect()
                        s
                    } catch (e1: Exception) {
                        try {
                            val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                            val fallbackSocket = method.invoke(device, 1) as BluetoothSocket
                            fallbackSocket.connect()
                            fallbackSocket
                        } catch (e2: Exception) {
                            throw e1
                        }
                    }
                    bluetoothSocket = socket

                    val ins = socket.inputStream
                    val outs = socket.outputStream

                    // Save the successful device address to settings
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        updatePairedDevice(device.name ?: "OBD2 Adapter", deviceAddress)
                    }

                    // Initialize ELM327 protocol commands
                    writeCommand(outs, "ATZ") // Reset
                    delay(800)
                    readResponse(ins)

                    writeCommand(outs, "ATE0") // Echo off
                    delay(150)
                    readResponse(ins)

                    writeCommand(outs, "ATL0") // Linefeeds off
                    delay(150)
                    readResponse(ins)

                    writeCommand(outs, "ATH0") // Headers off
                    delay(150)
                    readResponse(ins)

                    writeCommand(outs, "ATSP0") // Set Protocol to Auto
                    delay(200)
                    readResponse(ins)

                    writeCommand(outs, "ATS0") // Spaces off for ultra-compact, high-speed transmissions
                    delay(150)
                    readResponse(ins)

                    writeCommand(outs, "ATAT1") // Enable adaptive timing level 1 to dynamically lower transaction timeouts
                    delay(150)
                    readResponse(ins)

                    // Dispatch state updates and polling loop start back to Main thread
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isConnected = true
                        isConnecting = false
                        isSimulationActive = false
                        startBluetoothPollingLoop(ins, outs)
                    }
                }

            } catch (e: SecurityException) {
                bluetoothErrorMessage = "Bluetooth permission denied."
                isConnecting = false
                isConnected = false
            } catch (e: Exception) {
                bluetoothErrorMessage = "Failed to connect to OBD2 adapter: ${e.message}"
                isConnecting = false
                isConnected = false
                try {
                    bluetoothSocket?.close()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                bluetoothSocket = null
            }
        }
    }

    fun disconnectBluetooth() {
        isRunningBluetoothLoop = false
        try {
            bluetoothSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bluetoothSocket = null
        isConnected = false
    }

    private fun writeCommand(out: OutputStream, cmd: String) {
        try {
            out.write((cmd + "\r").toByteArray())
            out.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readResponse(ins: InputStream): String {
        val buffer = StringBuilder()
        val deadline = System.currentTimeMillis() + 2500 // 2.5 second timeout so it never hangs indefinitely
        try {
            while (System.currentTimeMillis() < deadline) {
                if (ins.available() > 0) {
                    val b = ins.read()
                    if (b == -1) break
                    val c = b.toChar()
                    if (c == '>') { // Command prompt character from ELM327
                        break
                    }
                    buffer.append(c)
                } else {
                    Thread.sleep(5)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return buffer.toString().trim()
    }

    private fun parseOBDResponse(pidId: String, response: String): Float? {
        val clean = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
        if (pidId == "battery") { // ATRV
            val numericString = clean.filter { it.isDigit() || it == '.' }
            return numericString.toFloatOrNull()
        }
        
        val pidHex = when (pidId) {
            "rpm" -> "410C"
            "coolant" -> "4105"
            "speed" -> "410D"
            "throttle" -> "4111"
            "load" -> "4104"
            "iat" -> "410F"
            "map" -> "410B"
            else -> ""
        }
        
        if (pidHex.isNotEmpty()) {
            val index = clean.indexOf(pidHex)
            if (index != -1 && index + pidHex.length < clean.length) {
                val dataPart = clean.substring(index + pidHex.length)
                try {
                    if (pidId == "rpm" && dataPart.length >= 4) {
                        val aa = dataPart.substring(0, 2).toInt(16)
                        val bb = dataPart.substring(2, 4).toInt(16)
                        return ((aa * 256) + bb) / 4f
                    } else if (dataPart.length >= 2) {
                        val aa = dataPart.substring(0, 2).toInt(16)
                        when (pidId) {
                            "coolant", "iat" -> return aa - 40f
                            "speed" -> return aa.toFloat()
                            "throttle", "load" -> return (aa * 100f) / 255f
                            "map" -> return aa.toFloat()
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parsing error
                }
            }
        }
        return null
    }

    private fun startBluetoothPollingLoop(ins: InputStream, outs: OutputStream) {
        isRunningBluetoothLoop = true
        if (bluetoothPollingJob?.isActive == true) return
        bluetoothPollingJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            while (isRunningBluetoothLoop) {
                val isConnectedVal = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { isConnected }
                if (!isConnectedVal) break

                try {
                    val startTime = System.currentTimeMillis()
                    
                    // Safely query state variables on the Main thread to prevent multi-thread Snapshot access exceptions
                    val (rate, currentPids, visibleIds) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        val r = if (settings.updateRate > 0) settings.updateRate else 10
                        val plist = pids.toList()
                        val vids = activeSensorIds.ifEmpty { plist.map { it.id }.toSet() }
                        Triple(r, plist, vids)
                    }
                    val delayMs = (1000 / rate).toLong()

                    currentPids.forEach { p ->
                        if (!isRunningBluetoothLoop) return@launch
                        if (!visibleIds.contains(p.id)) return@forEach // Skip non-visible PIDs for maximum Torque Pro-like responsiveness!

                        val cmd = when (p.id) {
                            "rpm" -> "010C"
                            "coolant" -> "0105"
                            "speed" -> "010D"
                            "battery" -> "ATRV"
                            "throttle" -> "0111"
                            "load" -> "0104"
                            "iat" -> "010F"
                            "map" -> "010B"
                            else -> ""
                        }

                        if (cmd.isNotEmpty()) {
                            writeCommand(outs, cmd)
                            val response = readResponse(ins)
                            val parsedVal = parseOBDResponse(p.id, response)
                            if (parsedVal != null) {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    val idx = pids.indexOfFirst { it.id == p.id }
                                    if (idx != -1) {
                                        pids[idx] = pids[idx].copy(value = parsedVal.coerceSafely(pids[idx].min, pids[idx].max))
                                    }
                                }
                            }
                        }
                        delay(5) // Reduced safety delay from 10ms to 5ms for maximum performance
                    }

                    // Perform UI state accumulation safely on the Main thread
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        val rpm = pids.find { it.id == "rpm" }?.value ?: 1500f
                        val coolant = pids.find { it.id == "coolant" }?.value ?: 90f
                        val speed = pids.find { it.id == "speed" }?.value ?: 0f
                        val throttle = pids.find { it.id == "throttle" }?.value ?: 10f

                        graphData.add(mapOf(
                            "rpm" to rpm,
                            "coolant" to coolant,
                            "speed" to speed,
                            "throttle" to throttle
                        ))
                        if (graphData.size > 50) graphData.removeAt(0)

                        if (isRecording) {
                            val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
                            val targetBattery = pids.find { it.id == "battery" }?.value ?: 14.1f
                            val targetLoad = pids.find { it.id == "load" }?.value ?: 50f
                            val targetMap = pids.find { it.id == "map" }?.value ?: 50f
                            currentLogLines.add("$timestamp,$rpm,$speed,$coolant,$targetBattery,$throttle,$targetLoad,$targetMap")
                        }
                    }

                    val elapsed = System.currentTimeMillis() - startTime
                    val sleepTime = (delayMs - elapsed).coerceIn(1, 1000)
                    delay(sleepTime) // Precise timing adjustment to sustain target update rate (Hz)
                } catch (e: Exception) {
                    e.printStackTrace()
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isConnected = false
                    }
                    break
                }
            }
        }
    }

    fun clearDtcs() {
        if (settings.connectionType == "Bluetooth" && isConnected) {
            viewModelScope.launch {
                isConnecting = true
                try {
                    val socket = bluetoothSocket
                    if (socket != null) {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            writeCommand(socket.outputStream, "04") // Clear codes
                            delay(1000)
                            readResponse(socket.inputStream)
                        }
                        dtcs.clear()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isConnecting = false
                }
            }
        } else {
            dtcs.clear()
            val misfireIdx = readiness.indexOfFirst { it.id == "misfire" }
            if (misfireIdx != -1) readiness[misfireIdx] = readiness[misfireIdx].copy(status = "READY")
        }
    }

    fun refreshDtcs() {
        if (settings.connectionType == "Bluetooth" && isConnected) {
            viewModelScope.launch {
                isConnecting = true
                try {
                    val socket = bluetoothSocket
                    if (socket != null) {
                        val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            writeCommand(socket.outputStream, "03") // Query DTCs
                            delay(1200)
                            readResponse(socket.inputStream)
                        }
                        val clean = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
                        val parsed = mutableListOf<DTC>()
                        
                        if (clean.startsWith("43") && clean.length >= 6) {
                            val numBytes = clean.substring(2)
                            for (k in 0 until numBytes.length step 4) {
                                if (k + 4 <= numBytes.length) {
                                    val hex = numBytes.substring(k, k + 4)
                                    if (hex == "0000") continue
                                    val cat = when (hex[0]) {
                                        '0' -> "P0"
                                        '1' -> "P1"
                                        '2' -> "P2"
                                        '3' -> "P3"
                                        '4' -> "C0"
                                        '5' -> "C1"
                                        '6' -> "C2"
                                        '7' -> "C3"
                                        '8' -> "B0"
                                        '9' -> "B1"
                                        'a' -> "B2"
                                        'b' -> "B3"
                                        'c' -> "U0"
                                        'd' -> "U1"
                                        'e' -> "U2"
                                        'f' -> "U3"
                                        else -> "P0"
                                    }
                                    val code = cat + hex.substring(1)
                                    val desc = when (code) {
                                        "P0171" -> "System Too Lean (Bank 1) - Fuel mixture too lean"
                                        "P0302" -> "Cylinder 2 Misfire Detected - Cylinder misfire ignition error"
                                        "P0420" -> "Catalyst System Efficiency Below Threshold - Catalytic converter efficiency low"
                                        else -> "Active Diagnostic DTC Code - Please check motorcycle electrical systems."
                                    }
                                    parsed.add(DTC(code, desc, "ERROR", "ECU Fault"))
                                }
                            }
                        }
                        dtcs.clear()
                        dtcs.addAll(parsed)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isConnecting = false
                }
            }
        } else {
            viewModelScope.launch {
                isConnecting = true
                delay(1000)
                isConnecting = false
                if (dtcs.isEmpty() && simMisfire) {
                    dtcs.add(DTC("P0302", "Cylinder 2 Misfire Detected", "ERROR", "Ignition System or Misfire"))
                }
            }
        }
    }

    fun deleteLog(name: String) {
        logs.removeAll { it.name == name }
    }

    fun toggleFavoriteLog(name: String) {
        val idx = logs.indexOfFirst { it.name == name }
        if (idx != -1) {
            logs[idx] = logs[idx].copy(isFavorite = !logs[idx].isFavorite)
        }
    }

    fun updateSettings(newSettings: OBD2Settings) {
        settings = newSettings
        prefs.edit().apply {
            putString("settings_connectionType", newSettings.connectionType)
            putString("settings_pairedDevice", newSettings.pairedDevice)
            putBoolean("settings_autoConnect", newSettings.autoConnect)
            putInt("settings_updateRate", newSettings.updateRate)
            putString("settings_profile", newSettings.profileName)
            apply()
        }
    }

    fun updatePairedDevice(name: String, address: String) {
        settings = settings.copy(pairedDevice = name)
        prefs.edit().apply {
            putString("settings_pairedDevice", name)
            putString("settings_pairedDeviceAddress", address)
            apply()
        }
    }

    fun toggleRecording() {
        if (isRecording) {
            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            val filename = "Log_${sdf.format(Date())}.csv"
            
            logs.add(0, LogFile(
                name = filename,
                date = dateStr,
                size = "${(currentLogLines.size * 80) / 1024} KB",
                isFavorite = false,
                content = currentLogLines.joinToString("\n")
            ))
            isRecording = false
            currentLogLines.clear()
        } else {
            isRecording = true
            currentLogLines.add("Timestamp,RPM,Speed,CoolantTemp,Voltage,Throttle,EngineLoad,MAP")
        }
    }

}

// Helper to parse hex colors safely in Compose
fun parseHexColor(hex: String?, defaultColor: androidx.compose.ui.graphics.Color): androidx.compose.ui.graphics.Color {
    if (hex == null) return defaultColor
    return try {
        val cleanHex = hex.replace("#", "")
        if (cleanHex.length == 6) {
            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor("#FF$cleanHex"))
        } else if (cleanHex.length == 8) {
            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor("#$cleanHex"))
        } else {
            defaultColor
        }
    } catch (e: Exception) {
        defaultColor
    }
}

// Get dynamic color based on current PID state thresholds
fun getPIDColor(pid: PID): androidx.compose.ui.graphics.Color {
    val v = pid.value
    return when {
        pid.warnMax != null && v >= pid.warnMax!! -> {
            parseHexColor(pid.colorHigh, androidx.compose.ui.graphics.Color(0xFFFF1744))
        }
        pid.warnMin != null && v <= pid.warnMin!! -> {
            parseHexColor(pid.colorLow, androidx.compose.ui.graphics.Color(0xFFFFD600))
        }
        else -> {
            parseHexColor(pid.colorOptimal, androidx.compose.ui.graphics.Color(0xFF00E5FF))
        }
    }
}

// Safe coercion helper to prevent IllegalArgumentException crashes when min >= max or value is NaN
fun Float.coerceSafely(min: Float, max: Float): Float {
    if (this.isNaN()) return if (min < max) min else max
    val realMin = if (min < max) min else max
    val realMax = if (min < max) max else min
    return this.coerceIn(realMin, realMax)
}
