package com.example.motoscanadv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.motoscanadv.OBDViewModel

@Composable
fun MoreScreen(
    viewModel: OBDViewModel,
    onNavigateToLogs: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showVehicleInfo by remember { mutableStateOf(false) }
    var showFreezeFrame by remember { mutableStateOf(false) }
    var showReadinessMonitor by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // HEADER
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = "MORE TOOLS & DIAGNOSTICS",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "EXTENDED VEHICLE DIAGNOSTIC FUNCTIONS",
                color = Color.Gray,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // DIAGNOSTIC SECTION
        Text(
            text = "DIAGNOSTIC",
            color = Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
        )

        // Readiness Monitor Button
        MoreMenuItemCard(
            title = "Readiness Monitor",
            desc = "Check status of emissions systems & sensors",
            iconColor = Color(0xFF4CAF50),
            icon = Icons.Filled.CheckCircle,
            onClick = { showReadinessMonitor = true }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Freeze Frame Button
        MoreMenuItemCard(
            title = "Freeze Frame",
            desc = "View sensor snapshot when DTC was triggered",
            iconColor = Color(0xFFFFB300),
            icon = Icons.Filled.Warning,
            onClick = { showFreezeFrame = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // TOOLS SECTION
        Text(
            text = "TOOLS",
            color = Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // O2 Sensor Monitor (PRO placeholder)
        MoreMenuItemCard(
            title = "O2 Sensor Monitor",
            desc = "Monitor real-time oxygen sensor voltage",
            iconColor = Color(0xFF00E5FF).copy(alpha = 0.5f),
            icon = Icons.Filled.Tune,
            isLocked = true,
            onClick = {}
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Vehicle Info Button
        MoreMenuItemCard(
            title = "Vehicle Info",
            desc = "Retrieve VIN number & active ECU type",
            iconColor = Color(0xFF2979FF),
            icon = Icons.Filled.Info,
            onClick = { showVehicleInfo = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // DATA LOGGING SECTION
        Text(
            text = "DATA LOGGING",
            color = Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Saved Logs Button
        MoreMenuItemCard(
            title = "Saved Logs",
            desc = "View and manage saved trip logs",
            iconColor = Color.LightGray,
            icon = Icons.Filled.List,
            onClick = onNavigateToLogs
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SETTINGS SECTION
        Text(
            text = "SETTINGS",
            color = Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Connection Settings
        MoreMenuItemCard(
            title = "Connection",
            desc = "${viewModel.settings.connectionType}: ${viewModel.settings.pairedDevice}",
            iconColor = Color(0xFF00E5FF),
            icon = Icons.Filled.Build,
            onClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(10.dp))

        // App Settings
        MoreMenuItemCard(
            title = "App Settings",
            desc = "Configure measurement units & options",
            iconColor = Color.Gray,
            icon = Icons.Filled.Settings,
            onClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(20.dp))
    }

    // VEHICLE INFO DIALOG MODAL
    if (showVehicleInfo) {
        Dialog(onDismissRequest = { showVehicleInfo = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151922)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E394A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VEHICLE INFO (ECU)",
                        color = Color(0xFF00E5FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = "Complete details read from K-Line/CAN bus",
                        color = Color.Gray,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B0D13), RoundedCornerShape(8.dp))
                            .border(0.5.dp, Color(0xFF1E2530), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VehicleInfoRow("Brand", "HONDA Motor")
                        VehicleInfoRow("Model", "ADV 150 (BoreUp 180cc)")
                        VehicleInfoRow("ECU Type", "Keihin FI (Japan)")
                        VehicleInfoRow("Protocol", "ISO 14230 (KWP2000)")
                        VehicleInfoRow("VIN Number", "MH1KF111XNK8845xx")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showVehicleInfo = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("OK", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }

    // FREEZE FRAME DIALOG MODAL
    if (showFreezeFrame) {
        Dialog(onDismissRequest = { showFreezeFrame = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151922)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E394A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FREEZE FRAME (SNAPSHOT)",
                        color = Color(0xFFFFB300),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = "Engine status and parameters when DTC P0302 was recorded",
                        color = Color.Gray,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (viewModel.dtcs.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0B0D13), RoundedCornerShape(8.dp))
                                .border(0.5.dp, Color(0xFF1E2530), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VehicleInfoRow("Triggered DTC", "P0302")
                            VehicleInfoRow("Engine Speed", "9,850 rpm")
                            VehicleInfoRow("Coolant Temp", "102 °C")
                            VehicleInfoRow("Vehicle Speed", "126 km/h")
                            VehicleInfoRow("Battery Voltage", "14.1 V")
                            VehicleInfoRow("Throttle Pos", "85.6 %")
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No freeze frame snapshot available because there are no active fault codes at this time.",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showFreezeFrame = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C212A)),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2E394A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CLOSE", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }

    // READINESS MONITOR DIALOG MODAL
    if (showReadinessMonitor) {
        Dialog(onDismissRequest = { showReadinessMonitor = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151922)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E394A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EMISSION READINESS MONITORS",
                        color = Color(0xFF00E5FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = "Emissions system readiness status (OBD2)",
                        color = Color.Gray,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        viewModel.readiness.forEach { item ->
                            val isReady = item.status == "READY"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0B0D13), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.name, color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    Text(item.name, color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                }

                                Text(
                                    text = item.status,
                                    color = if (isReady) Color(0xFF00E676) else Color(0xFFFF1744),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(
                                            if (isReady) Color(0xFF00E676).copy(alpha = 0.12f) else Color(0xFFFF1744).copy(alpha = 0.12f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showReadinessMonitor = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("BACK", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun MoreMenuItemCard(
    title: String,
    desc: String,
    iconColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked) { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13171F)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222935)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        color = if (isLocked) Color.Gray else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = desc,
                        color = Color.Gray,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (isLocked) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1A222F), RoundedCornerShape(4.dp))
                        .border(0.5.dp, Color(0xFF2E394A), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PRO",
                        color = Color.LightGray,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "Go",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun VehicleInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            color = Color.Gray,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
