package com.example.motoscanadv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motoscanadv.DTC
import com.example.motoscanadv.OBDViewModel

@Composable
fun DtcScreen(viewModel: OBDViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .padding(16.dp)
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ECU DIAGNOSTICS & DTC",
                    color = Color(0xFF00E5FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "READ AND CLEAR FAULT CODES",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { viewModel.refreshDtcs() },
                    modifier = Modifier.background(Color(0xFF131720), RoundedCornerShape(4.dp))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.LightGray)
                }
                IconButton(
                    onClick = { viewModel.clearDtcs() },
                    modifier = Modifier.background(Color(0xFF131720), RoundedCornerShape(4.dp))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // STATUS PILL SUMMARY
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (viewModel.dtcs.isEmpty()) Color(0xFF0B1B15) else Color(0xFF2A0D10)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, 
                if (viewModel.dtcs.isEmpty()) Color(0xFF00E676) else Color(0xFFFF1744)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (viewModel.dtcs.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (viewModel.dtcs.isEmpty()) Color(0xFF00E676) else Color(0xFFFF1744),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (viewModel.dtcs.isEmpty()) "SYSTEM STABLE" else "DTC ALERTS ACTIVE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (viewModel.dtcs.isEmpty()) "0 stored ECU diagnostic faults detected." else "${viewModel.dtcs.size} stored trouble faults active in engine module.",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // LOOPS DTC LIST
        if (viewModel.dtcs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color(0xFF1A222B), RoundedCornerShape(8.dp))
                    .background(Color(0xFF0E1117)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No fault codes found on ECU.", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Safe riding!", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = viewModel.dtcs,
                    key = { it.code }
                ) { dtc ->
                    DtcCard(dtc = dtc)
                }
            }
        }

    }
}

@Composable
fun DtcCard(dtc: DTC) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B24)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3443)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dtc.code,
                    color = Color(0xFFFF1744),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF2E1014))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = dtc.status,
                        color = Color(0xFFFF1744),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dtc.description,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "SYSTEM: ${dtc.category.uppercase()}",
                color = Color.Gray,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
