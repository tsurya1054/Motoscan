package com.example.motoscanadv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.motoscanadv.OBDViewModel
import com.example.motoscanadv.PID
import com.example.motoscanadv.ui.components.DialGauge
import com.example.motoscanadv.getPIDColor
import com.example.motoscanadv.parseHexColor
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: OBDViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("motoscan_prefs", android.content.Context.MODE_PRIVATE) }
    
    var mainPidId by remember {
        mutableStateOf(prefs.getString("dashboard_main_pid", "rpm") ?: "rpm")
    }
    
    var activeSlots by remember {
        mutableStateOf(
            prefs.getString("dashboard_active_slots", "main,speed,coolant,battery,throttle")
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.let { parsed ->
                    if (!parsed.contains("main")) {
                        listOf("main") + parsed
                    } else {
                        parsed
                    }
                }
                ?: listOf("main", "speed", "coolant", "battery", "throttle")
        )
    }
    
    var gridColumns by remember {
        mutableStateOf(prefs.getInt("dashboard_grid_columns", 2))
    }

    var widgetSizes by remember {
        mutableStateOf(
            prefs.getString("dashboard_widget_sizes", "")
                ?.split(";")
                ?.filter { it.contains(":") }
                ?.associate { 
                    val parts = it.split(":")
                    parts[0] to parts[1]
                }
                ?: emptyMap()
        )
    }

    var mainDialSize by remember {
        mutableStateOf(prefs.getString("dashboard_main_dial_size", "medium") ?: "medium")
    }

    var widgetScales by remember {
        mutableStateOf(
            prefs.getString("dashboard_widget_scales", "")
                ?.split(";")
                ?.filter { it.contains(":") }
                ?.associate { 
                    val parts = it.split(":")
                    parts[0] to (parts[1].toFloatOrNull() ?: 1.0f)
                }
                ?: emptyMap()
        )
    }
    
    var editLayoutMode by remember { mutableStateOf(false) }
    var showAddWidgetDialog by remember { mutableStateOf(false) }
    var showConnectDialog by remember { mutableStateOf(false) }

    // Dialog & configuration selection states
    var activeSlotToChange by remember { mutableStateOf<String?>(null) } // "main", "slot_X"
    var pidToConfigureSettings by remember { mutableStateOf<PID?>(null) }

    val saveActiveSlots = { slots: List<String> ->
        activeSlots = slots
        prefs.edit().putString("dashboard_active_slots", slots.joinToString(",")).apply()
    }
    
    val saveGridColumns = { cols: Int ->
        gridColumns = cols
        prefs.edit().putInt("dashboard_grid_columns", cols).apply()
    }

    val saveMainPid = { pidId: String ->
        mainPidId = pidId
        prefs.edit().putString("dashboard_main_pid", pidId).apply()
    }

    val saveMainDialSize = { sz: String ->
        mainDialSize = sz
        prefs.edit().putString("dashboard_main_dial_size", sz).apply()
    }

    val saveWidgetSize = { slotPidId: String, size: String ->
        val updated = widgetSizes.toMutableMap()
        updated[slotPidId] = size
        widgetSizes = updated
        val serialized = updated.entries.joinToString(";") { "${it.key}:${it.value}" }
        prefs.edit().putString("dashboard_widget_sizes", serialized).apply()
    }

    val saveWidgetScale = { slotPidId: String, scale: Float ->
        val updated = widgetScales.toMutableMap()
        val nextScale = (Math.round(scale * 10f) / 10f).coerceIn(0.4f, 2.0f)
        updated[slotPidId] = nextScale
        widgetScales = updated
        val serialized = updated.entries.joinToString(";") { "${it.key}:${it.value}" }
        prefs.edit().putString("dashboard_widget_scales", serialized).apply()
    }
    
    // Track active sensors in ViewModel to optimize Bluetooth polling rate!
    LaunchedEffect(mainPidId, activeSlots) {
        viewModel.activeSensorIds = (setOf(mainPidId) + activeSlots.filter { it != "main" }).toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HEAD BAR STATUS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MotoScan ADV",
                    color = Color(0xFF00E5FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (viewModel.isConnected) "LIVE OBD2 · ${viewModel.settings.pairedDevice}" else if (viewModel.isSimulationActive) "DEMO SIMULASI RUNNING" else "OFFLINE · STANDBY",
                    color = if (viewModel.isConnected) Color(0xFF00C853) else if (viewModel.isSimulationActive) Color(0xFF00E5FF) else Color.Gray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Record telemetry button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (viewModel.isRecording) Color(0xFFFF1744) else Color(0xFF1E2530))
                        .clickable { viewModel.toggleRecording() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (viewModel.isRecording) "● REC" else "REC",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Connected Status / Connect Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                viewModel.isConnected -> Color(0xFF00C853)
                                viewModel.isConnecting -> Color(0xFFFF9100)
                                viewModel.isSimulationActive -> Color(0xFF00E5FF)
                                else -> Color(0xFFFF3D00)
                            }
                        )
                        .clickable { showConnectDialog = true }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = when {
                            viewModel.isConnected -> "CONNECTED ▾"
                            viewModel.isConnecting -> "CONNECTING..."
                            viewModel.isSimulationActive -> "DEMO ▾"
                            else -> "CONNECT ▾"
                        },
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // LAYOUT CONTROLS ROW
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "EDIT LAYOUT",
                    color = if (editLayoutMode) Color(0xFF00E5FF) else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = editLayoutMode,
                    onCheckedChange = { editLayoutMode = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E5FF),
                        checkedTrackColor = Color(0xFF142A35)
                    ),
                    modifier = Modifier.scale(0.8f)
                )
            }
            
            if (editLayoutMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("COLS:", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    listOf(1, 2, 3).forEach { cols ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (gridColumns == cols) Color(0xFF00E5FF) else Color(0xFF1C1F26))
                                .clickable { saveGridColumns(cols) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$cols",
                                color = if (gridColumns == cols) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        if (editLayoutMode) {
            Button(
                onClick = { showAddWidgetDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF142A35)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Widget",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("ADD WIDGET CARD", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        } else {
            // Help hint banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13171F)),
                border = borderStroke(Color(0xFF1E252E)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tap widget to replace sensor / edit boundaries. Enable Edit Layout to customize grid.",
                        color = Color.LightGray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // DYNAMIC UNIFIED GRID LAYOUT
        val rows = remember(activeSlots, widgetSizes, mainDialSize, gridColumns) {
            val result = mutableListOf<List<Pair<String, Int>>>()
            var currentRow = mutableListOf<Pair<String, Int>>()
            var currentSpanUsed = 0

            activeSlots.forEachIndexed { index, slotId ->
                val size = if (slotId == "main") mainDialSize else (widgetSizes[slotId] ?: "small")
                val itemSpan = when (gridColumns) {
                    1 -> 6
                    2 -> if (size == "small") 3 else 6
                    3 -> when (size) {
                        "small" -> 2
                        "medium" -> 4
                        else -> 6
                    }
                    else -> 6
                }

                if (currentSpanUsed + itemSpan > 6 && currentRow.isNotEmpty()) {
                    result.add(currentRow)
                    currentRow = mutableListOf()
                    currentSpanUsed = 0
                }

                currentRow.add(slotId to index)
                currentSpanUsed += itemSpan
            }

            if (currentRow.isNotEmpty()) {
                result.add(currentRow)
            }
            result
        }

        rows.forEach { rowItems ->
            val rowSpanSum = rowItems.sumOf { item ->
                val slotId = item.first
                val size = if (slotId == "main") mainDialSize else (widgetSizes[slotId] ?: "small")
                val spanVal: Int = when (gridColumns) {
                    1 -> 6
                    2 -> if (size == "small") 3 else 6
                    3 -> when (size) {
                        "small" -> 2
                        "medium" -> 4
                        else -> 6
                    }
                    else -> 6
                }
                spanVal
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { (slotPidId, actualSlotIndex) ->
                    val size = if (slotPidId == "main") mainDialSize else (widgetSizes[slotPidId] ?: "small")
                    val scale = widgetScales[slotPidId] ?: 1.0f
                    val itemSpan = when (gridColumns) {
                        1 -> 6
                        2 -> if (size == "small") 3 else 6
                        3 -> when (size) {
                            "small" -> 2
                            "medium" -> 4
                            else -> 6
                        }
                        else -> 6
                    }

                    Box(
                        modifier = Modifier
                            .weight(itemSpan.toFloat())
                            .layoutScale(scale),
                        contentAlignment = Alignment.Center
                    ) {
                        if (slotPidId == "main") {
                            DashboardMainDial(
                                viewModel = viewModel,
                                pidId = mainPidId,
                                size = mainDialSize,
                                onSizeChange = saveMainDialSize,
                                editLayoutMode = editLayoutMode,
                                onClick = { activeSlotToChange = "main" },
                                onEditScale = { pidToConfigureSettings = it }
                            )
                        } else {
                            DashboardSlotCard(
                                viewModel = viewModel,
                                pidId = slotPidId,
                                size = size,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { activeSlotToChange = "slot_$actualSlotIndex" },
                                onEditScale = { pidToConfigureSettings = it }
                            )
                        }
                        
                        if (editLayoutMode) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.95f), RoundedCornerShape(4.dp))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // S / M / L Size Buttons Selector
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("S" to "small", "M" to "medium", "L" to "large").forEach { (label, sz) ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(if (size == sz) Color(0xFF00E5FF) else Color(0xFF090A0F))
                                                .clickable { 
                                                    if (slotPidId == "main") {
                                                        saveMainDialSize(sz)
                                                    } else {
                                                        saveWidgetSize(slotPidId, sz)
                                                    }
                                                }
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (size == sz) Color.Black else Color.Gray,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(1.dp))

                                // Scale Decrease
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Zoom Out",
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { saveWidgetScale(slotPidId, scale - 0.1f) }
                                )
                                Text(
                                    text = "${Math.round(scale * 100)}%",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                // Scale Increase
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Zoom In",
                                    tint = Color.Green,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { saveWidgetScale(slotPidId, scale + 0.1f) }
                                )

                                Spacer(modifier = Modifier.width(1.dp))

                                // Move Up / Left arrow
                                if (actualSlotIndex > 0) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Move Left",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                val mutable = activeSlots.toMutableList()
                                                val tmp = mutable[actualSlotIndex]
                                                mutable[actualSlotIndex] = mutable[actualSlotIndex - 1]
                                                mutable[actualSlotIndex - 1] = tmp
                                                saveActiveSlots(mutable)
                                            }
                                    )
                                }
                                
                                // Move Down / Right arrow
                                if (actualSlotIndex < activeSlots.size - 1) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Move Right",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                val mutable = activeSlots.toMutableList()
                                                val tmp = mutable[actualSlotIndex]
                                                mutable[actualSlotIndex] = mutable[actualSlotIndex + 1]
                                                mutable[actualSlotIndex + 1] = tmp
                                                saveActiveSlots(mutable)
                                            }
                                    )
                                }
                                
                                // Delete Widget
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Widget",
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            val mutable = activeSlots.toMutableList()
                                            mutable.removeAt(actualSlotIndex)
                                            saveActiveSlots(mutable)
                                        }
                                )
                            }
                        }
                    }
                }
                
                // Add empty spacer if row is incomplete
                if (rowSpanSum < 6) {
                    Spacer(modifier = Modifier.weight((6 - rowSpanSum).toFloat()))
                }
            }
        }
    }

    // BLUETOOTH OBD2 CONNECT / DEVICE PICKER DIALOG (TORQUE PRO STYLE)
    if (showConnectDialog) {
        Dialog(onDismissRequest = { showConnectDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1E2530), RoundedCornerShape(12.dp)),
                color = Color(0xFF11141A)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "KONEKSI BLUETOOTH OBD2 (ELM327)",
                        color = Color(0xFF00E5FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (viewModel.isConnecting) {
                        CircularProgressIndicator(
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Menghubungkan ke modul OBD2...",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else if (viewModel.isConnected) {
                        Text(
                            text = "STATUS: TERHUBUNG KE MOTOR",
                            color = Color(0xFF00C853),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Perangkat: ${viewModel.settings.pairedDevice}",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.disconnectBluetooth()
                                showConnectDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3D00)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("PUTUSKAN KONEKSI (DISCONNECT)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        // Disconnected state
                        val pairedList = remember { viewModel.getPairedDevices() }
                        Text(
                            text = "PILIH MODUL OBD2 YANG SUDAH DIPASANGKAN:",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (pairedList.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF181E27)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Belum ada modul OBD2 Bluetooth yang di-pairing ke HP ini.\n\nCara pasangkan:\n1. Buka Pengaturan HP > Bluetooth\n2. Cari perangkat 'OBDII' / 'ELM327' (PIN 1234 atau 0000)\n3. Setelah paired, buka lagi aplikasi ini dan pilih perangkat.",
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        } else {
                            pairedList.forEach { dev ->
                                val devName = dev["name"] ?: "OBD2"
                                val devAddr = dev["address"] ?: ""
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181E27)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable {
                                            viewModel.connectBluetooth(devAddr)
                                            showConnectDialog = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(devName, color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text(devAddr, color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        Text("CONNECT", color = Color(0xFF00C853), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Simulation / Demo button
                        Button(
                            onClick = {
                                viewModel.toggleSimulationMode()
                                showConnectDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.isSimulationActive) Color(0xFFFF9100) else Color(0xFF1E2530)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (viewModel.isSimulationActive) "STOP SIMULASI" else "MODE DEMO / TEST GAUGES",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (viewModel.bluetoothErrorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = viewModel.bluetoothErrorMessage ?: "",
                            color = Color(0xFFFF5252),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { showConnectDialog = false }) {
                        Text("TUTUP", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }

    // ADD WIDGET CARD DIALOG
    if (showAddWidgetDialog) {
        val availablePids = viewModel.pids.filter { !activeSlots.contains(it.id) }
        val isMainAvailable = !activeSlots.contains("main")
        Dialog(onDismissRequest = { showAddWidgetDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151922)),
                border = borderStroke(Color(0xFF2E394A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ADD WIDGET INDICATOR",
                        color = Color(0xFF00E5FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    if (availablePids.isEmpty() && !isMainAvailable) {
                        Text(
                            text = "All indicators are already displayed on the dashboard.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column {
                            if (isMainAvailable) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            val mutable = activeSlots.toMutableList()
                                            mutable.add(0, "main") // add back to start
                                            saveActiveSlots(mutable)
                                            showAddWidgetDialog = false
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("MAIN SWEEP DIAL GAUGE", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        Text("Primary Circular Dial | System Gauge", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Divider(color = Color(0xFF222935), thickness = 0.5.dp)
                            }

                            availablePids.forEach { pid ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            val mutable = activeSlots.toMutableList()
                                            mutable.add(pid.id)
                                            saveActiveSlots(mutable)
                                            showAddWidgetDialog = false
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(pid.name, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        Text(pid.shortName + " | " + pid.category, color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Divider(color = Color(0xFF222935), thickness = 0.5.dp)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = { showAddWidgetDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("CLOSE", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }

    // SLOT CHANGE REPLACEMENT MODAL DIALOG
    if (activeSlotToChange != null) {
        Dialog(onDismissRequest = { activeSlotToChange = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151922)),
                border = borderStroke(Color(0xFF2E394A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SELECT SENSOR INDICATOR",
                        color = Color(0xFF00E5FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    viewModel.pids.forEach { pid ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    val currentSlot = activeSlotToChange!!
                                    if (currentSlot == "main") {
                                        saveMainPid(pid.id)
                                    } else if (currentSlot.startsWith("slot_")) {
                                        val idx = currentSlot.substringAfter("slot_").toIntOrNull()
                                        if (idx != null && idx in activeSlots.indices) {
                                            val mutable = activeSlots.toMutableList()
                                            mutable[idx] = pid.id
                                            saveActiveSlots(mutable)
                                        }
                                    }
                                    activeSlotToChange = null
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(pid.name, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text(pid.shortName + " | " + pid.category, color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            Text("${String.format(Locale.US, "%.1f", pid.value)} ${pid.unit}", color = Color(0xFF00E5FF), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        Divider(color = Color(0xFF222935), thickness = 0.5.dp)
                    }
                }
            }
        }
    }

    // PID SCALE CONFIGURATOR CUSTOMIZER DIALOG
    if (pidToConfigureSettings != null) {
        val editingPid = pidToConfigureSettings!!
        
        var minText by remember { mutableStateOf(editingPid.min.toString()) }
        var maxText by remember { mutableStateOf(editingPid.max.toString()) }
        var warnMinText by remember { mutableStateOf(editingPid.warnMin?.toString() ?: "") }
        var warnMaxText by remember { mutableStateOf(editingPid.warnMax?.toString() ?: "") }

        var optimalColorHex by remember { mutableStateOf(editingPid.colorOptimal ?: "#00E5FF") }
        var lowColorHex by remember { mutableStateOf(editingPid.colorLow ?: "#FFD600") }
        var highColorHex by remember { mutableStateOf(editingPid.colorHigh ?: "#FF1744") }

        var saveError by remember { mutableStateOf<String?>(null) }

        Dialog(onDismissRequest = { pidToConfigureSettings = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151922)),
                border = borderStroke(Color(0xFF2E394A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "EDIT SCALE: ${editingPid.shortName}",
                        color = Color(0xFF00E5FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Set minimum, maximum boundaries, redline limit, and sensor color palette.",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Error Alert
                    if (saveError != null) {
                        Text(
                            text = saveError!!,
                            color = Color.Red,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Minimum Scale Limit
                    Text("Scale Minimum Limit (${editingPid.unit})", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    TextField(
                        value = minText,
                        onValueChange = { minText = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1D222E),
                            unfocusedContainerColor = Color(0xFF1D222E),
                            focusedIndicatorColor = Color(0xFF00E5FF)
                        )
                    )

                    // Maximum Scale Limit
                    Text("Scale Maximum Limit (${editingPid.unit})", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    TextField(
                        value = maxText,
                        onValueChange = { maxText = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1D222E),
                            unfocusedContainerColor = Color(0xFF1D222E),
                            focusedIndicatorColor = Color(0xFF00E5FF)
                        )
                    )

                    // Warning Minimum Threshold
                    Text("Warning Minimum Limit (Leave empty if none)", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    TextField(
                        value = warnMinText,
                        onValueChange = { warnMinText = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1D222E),
                            unfocusedContainerColor = Color(0xFF1D222E),
                            focusedIndicatorColor = Color(0xFF00E5FF)
                        )
                    )

                    // Warning Maximum Threshold (Redline)
                    Text("Warning Maximum Limit (Redline)", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    TextField(
                        value = warnMaxText,
                        onValueChange = { warnMaxText = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1D222E),
                            unfocusedContainerColor = Color(0xFF1D222E),
                            focusedIndicatorColor = Color(0xFF00E5FF)
                        )
                    )

                    // Color Config
                    Text("Normal Color (Hex)", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    TextField(
                        value = optimalColorHex,
                        onValueChange = { optimalColorHex = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1D222E),
                            unfocusedContainerColor = Color(0xFF1D222E),
                            focusedIndicatorColor = Color(0xFF00E5FF)
                        )
                    )

                    Text("Redline / Danger Color (Hex)", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    TextField(
                        value = highColorHex,
                        onValueChange = { highColorHex = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1D222E),
                            unfocusedContainerColor = Color(0xFF1D222E),
                            focusedIndicatorColor = Color(0xFF00E5FF)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { pidToConfigureSettings = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E242C)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val minVal = minText.toFloatOrNull()
                                val maxVal = maxText.toFloatOrNull()
                                val wMinVal = warnMinText.toFloatOrNull()
                                val wMaxVal = warnMaxText.toFloatOrNull()

                                if (minVal == null || maxVal == null) {
                                    saveError = "Minimum & maximum limits must be valid numbers."
                                    return@Button
                                }
                                if (minVal >= maxVal) {
                                    saveError = "Minimum limit must be less than maximum limit."
                                    return@Button
                                }

                                viewModel.updatePIDConfig(
                                    id = editingPid.id,
                                    min = minVal,
                                    max = maxVal,
                                    warnMin = wMinVal,
                                    warnMax = wMaxVal,
                                    colorOptimal = optimalColorHex,
                                    colorLow = lowColorHex,
                                    colorHigh = highColorHex
                                )
                                pidToConfigureSettings = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMainDial(
    viewModel: OBDViewModel,
    pidId: String,
    size: String,
    onSizeChange: (String) -> Unit,
    editLayoutMode: Boolean,
    onClick: () -> Unit,
    onEditScale: (PID) -> Unit,
    modifier: Modifier = Modifier
) {
    val pid = viewModel.pids.find { it.id == pidId } ?: viewModel.pids.firstOrNull() ?: return
    val dialDp = when (size) {
        "small" -> 175.dp
        "large" -> 285.dp
        else -> 230.dp
    }
    Box(
        modifier = modifier
            .size(dialDp)
            .clickable { onClick() },
        contentAlignment = Alignment.BottomEnd
    ) {
        DialGauge(
            value = pid.value,
            min = pid.min,
            max = pid.max,
            title = pid.name.uppercase(),
            unit = pid.unit,
            modifier = Modifier.fillMaxSize(),
            mainColor = parseHexColor(pid.colorOptimal, Color(0xFF00E5FF)),
            warnThreshold = pid.warnMax ?: (pid.max * 0.85f),
            warnColor = parseHexColor(pid.colorHigh, Color(0xFFFF1744))
        )

        // Overlay controls row at the bottom right corner of the gauge box
        Row(
            modifier = Modifier
                .offset(x = (-4).dp, y = (-12).dp)
                .background(Color.Black.copy(alpha = 0.92f), RoundedCornerShape(20.dp))
                .border(androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(20.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (editLayoutMode) {
                listOf("S" to "small", "M" to "medium", "L" to "large").forEach { (label, sz) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (size == sz) Color(0xFF00E5FF) else Color(0xFF151922))
                            .clickable { onSizeChange(sz) }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (size == sz) Color.Black else Color.Gray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(modifier = Modifier.width(1.dp))
            }

            IconButton(
                onClick = { onEditScale(pid) },
                modifier = Modifier
                    .background(Color(0xFF1E2530), RoundedCornerShape(20.dp))
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Edit Scale",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun Sparkline(points: List<Float>, color: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val min = points.minOrNull() ?: 0f
        val max = points.maxOrNull() ?: 0f
        val range = if (max - min == 0f) 1f else max - min
        
        val width = size.width
        val height = size.height
        val path = androidx.compose.ui.graphics.Path()
        
        points.forEachIndexed { idx, value ->
            val x = (idx.toFloat() / (points.size - 1)) * width
            val y = height - ((value - min) / range) * (height - 8.dp.toPx()) - 4.dp.toPx()
            if (idx == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@Composable
fun MiniArcGauge(value: Float, min: Float, max: Float, color: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val pct = ((value - min) / (if (max - min == 0f) 1f else max - min)).coerceIn(0f, 1f)
        val arcWidth = size.width
        val strokeWidth = 5.dp.toPx()
        
        // Background arc
        drawArc(
            color = Color.White.copy(alpha = 0.08f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(arcWidth, arcWidth)
        )
        
        // Active arc
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f * pct,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth + 1.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(arcWidth, arcWidth)
        )
    }
}

@Composable
fun DashboardSlotCard(
    viewModel: OBDViewModel,
    pidId: String,
    size: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onEditScale: (PID) -> Unit
) {
    val pid = viewModel.pids.find { it.id == pidId } ?: return
    val valColor = getPIDColor(pid)
    val isAlert = (pid.warnMax != null && pid.value >= pid.warnMax!!) || (pid.warnMin != null && pid.value <= pid.warnMin!!)

    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = if (isAlert) 1.5.dp else 1.dp,
                color = if (isAlert) valColor else Color(0xFF222935),
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13161C)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            // Settings Icon Trigger
            IconButton(
                onClick = { onEditScale(pid) },
                modifier = Modifier
                    .padding(4.dp)
                    .size(20.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Edit Scale",
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(11.dp)
                )
            }

            val formattedValue = if (pid.value % 1f == 0f) {
                String.format(Locale.US, "%.0f", pid.value)
            } else {
                String.format(Locale.US, "%.1f", pid.value)
            }

            when (size) {
                "medium" -> {
                    // Medium Widget: Left is value, Right is real-time Sparkline
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = pid.shortName.uppercase(),
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isAlert) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = valColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formattedValue,
                                    color = valColor,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = pid.unit,
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .background(valColor.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                    .border(0.5.dp, valColor.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isAlert) "ALERT" else "OPTIMAL",
                                    color = valColor,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Sparkline Canvas
                        val historyPoints = remember(viewModel.graphData.size) {
                            viewModel.graphData.mapNotNull { it[pid.id] }.takeLast(15)
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                .padding(6.dp)
                        ) {
                            Text(
                                "SPARKLINE",
                                color = Color.Gray,
                                fontSize = 7.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                            Sparkline(
                                points = historyPoints,
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.size(width = 85.dp, height = 28.dp)
                            )
                        }
                    }
                }
                "large" -> {
                    // Large Widget: Sweep Arc Gauge + Numeric details + Min/Max Status row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pid.shortName.uppercase(),
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            if (isAlert) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = valColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Native Arc Gauge
                        MiniArcGauge(
                            value = pid.value,
                            min = pid.min,
                            max = pid.max,
                            color = valColor,
                            modifier = Modifier.size(width = 75.dp, height = 38.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = formattedValue,
                                color = valColor,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = pid.unit,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(5.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MIN: ${String.format(Locale.US, "%.0f", pid.min)}",
                                color = Color.Gray,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isAlert) "ALERT" else "OPTIMAL",
                                color = valColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "MAX: ${String.format(Locale.US, "%.0f", pid.max)}",
                                color = Color.Gray,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                else -> {
                    // Default Small Widget layout
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pid.shortName.uppercase(),
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            if (isAlert) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = valColor,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formattedValue,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = pid.unit,
                            color = valColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun borderStroke(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)

fun Modifier.layoutScale(scale: Float): Modifier = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val width = (placeable.width * scale).toInt()
    val height = (placeable.height * scale).toInt()
    layout(width, height) {
        placeable.placeWithLayer(
            x = ((width - placeable.width) / 2f).toInt(),
            y = ((height - placeable.height) / 2f).toInt()
        ) {
            scaleX = scale
            scaleY = scale
        }
    }
}
