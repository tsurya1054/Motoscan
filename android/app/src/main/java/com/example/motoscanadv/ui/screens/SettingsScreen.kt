package com.example.motoscanadv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motoscanadv.OBD2Settings
import com.example.motoscanadv.OBDViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: OBDViewModel) {
    val cur = viewModel.settings
    var isEditingProfile by remember { mutableStateOf(false) }
    var profileInput by remember { mutableStateOf(cur.profileName) }
    val pairedList = remember { mutableStateListOf<Map<String, String>>() }
    var refreshCount by remember { mutableStateOf(0) }

    LaunchedEffect(cur.connectionType, refreshCount) {
        if (cur.connectionType == "Bluetooth") {
            pairedList.clear()
            pairedList.addAll(viewModel.getPairedDevices())
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP PREFERENCES HEADER
        item {
            Text(
                text = "PREFERENCES & ADAPTER SETUP",
                color = Color(0xFF00E5FF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "CONFIGURATION OPTIONS FOR ELM327 SCANNER",
                color = Color.Gray,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // BIKE ENGINE PROFILE CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13171F)),
                border = borderStroke(Color(0xFF222935)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("MOTORCYCLE ECU PROFILE", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    if (isEditingProfile) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = profileInput,
                                onValueChange = { profileInput = it },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    viewModel.updateSettings(cur.copy(profileName = profileInput))
                                    isEditingProfile = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("OK", color = Color.Black, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cur.profileName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(
                                text = "📝 EDIT",
                                color = Color(0xFF00E5FF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.clickable { isEditingProfile = true }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Matched Injector Mapping: Keihin ADV v1.8, Bore-Up 180cc.", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // CONNECTION TYPE (Always Bluetooth)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13171F)),
                border = borderStroke(Color(0xFF222935)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("TELEMETRY CONNECTION MODE", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = "REAL OBD2 (BLUETOOTH)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Directly connected using ELM327 v2.1 adapter via Bluetooth connection.", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // BLUETOOTH STATUS CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13171F)),
                border = borderStroke(Color(0xFF222935)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("OBD2 BLUETOOTH STATUS", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val statusColor = when {
                            viewModel.isConnecting -> Color(0xFFFFD600)
                            viewModel.isConnected -> Color(0xFF00E676)
                            else -> Color(0xFFFF1744)
                        }
                        val statusText = when {
                            viewModel.isConnecting -> "CONNECTING..."
                            viewModel.isConnected -> "CONNECTED TO ECU"
                            else -> "DISCONNECTED"
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )

                        if (viewModel.isConnected) {
                            Button(
                                onClick = { viewModel.disconnectBluetooth() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1014)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("DISCONNECT", color = Color(0xFFFF1744), fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    viewModel.bluetoothErrorMessage?.let { errMsg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errMsg,
                            color = Color(0xFFFF1744),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT OBD2 BLUETOOTH DEVICE",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "🔄 RE-SCAN",
                    color = Color(0xFF00E5FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clickable { refreshCount++ }
                        .padding(top = 4.dp)
                )
            }
        }
        if (pairedList.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1117)),
                    border = borderStroke(Color(0xFF1E252E)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No paired Bluetooth devices found. Please pair your ELM327 adapter first in Android Settings.",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else {
            items(
                items = pairedList,
                key = { it["address"] ?: "" }
            ) { device ->
                val isSelected = cur.pairedDevice == device["name"]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            device["name"]?.let { name ->
                                device["address"]?.let { address ->
                                    viewModel.updatePairedDevice(name, address)
                                    viewModel.connectBluetooth(address)
                                }
                            }
                         },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF142A35) else Color(0xFF13171F)
                    ),
                    border = if (isSelected) borderStroke(Color(0xFF00E5FF)) else borderStroke(Color(0xFF222935)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = device["name"] ?: "Unknown OBD2 Device",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = device["address"] ?: "00:00:00:00:00:00",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (isSelected) {
                            Text(
                                text = "PAIRED & CONNECTING",
                                color = Color(0xFF00E5FF),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // REGIONAL & UNITS DISPLAY
        item {
            Text(
                text = "REGIONAL & UNITS DISPLAY",
                color = Color.Gray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13171F)),
                border = borderStroke(Color(0xFF222935)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Measurement Units", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { viewModel.updateSettings(cur.copy(units = "Metric")) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (cur.units == "Metric") Color(0xFF00E5FF) else Color(0xFF1C222C)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Metric", color = if (cur.units == "Metric") Color.Black else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.updateSettings(cur.copy(units = "Imperial")) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (cur.units == "Imperial") Color(0xFF00E5FF) else Color(0xFF1C222C)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Imperial", color = if (cur.units == "Imperial") Color.Black else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("App Language", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { viewModel.updateSettings(cur.copy(language = "Indonesian")) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (cur.language == "Indonesian") Color(0xFF00E5FF) else Color(0xFF1C222C)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Indo", color = if (cur.language == "Indonesian") Color.Black else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.updateSettings(cur.copy(language = "English")) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (cur.language == "English") Color(0xFF00E5FF) else Color(0xFF1C222C)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Eng", color = if (cur.language == "English") Color.Black else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ADV SYSTEM INFO PANEL
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1218)),
                border = borderStroke(Color(0xFF1C2631)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ECU PROFILE: HONDA KEIHIN ADV 150/160",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Compatible with ELM327 K-Line / CAN-Bus ISO-15765 protocols.",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

private fun borderStroke(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)
