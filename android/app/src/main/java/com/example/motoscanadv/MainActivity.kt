package com.example.motoscanadv

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.motoscanadv.ui.screens.*

// ========================================================
// CENTRAL COMPONENT ACTIVITY & MODULAR ROUTING ENTRY POINT
// ========================================================

class MainActivity : ComponentActivity() {
    private val viewModel: OBDViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            // Apply a premium, integrated dark palette theme
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF090A0F),
                    surface = Color(0xFF13171F),
                    primary = Color(0xFF00E5FF),
                    secondary = Color(0xFF00C853)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF090A0F)
                ) {
                    MainContainer(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainContainer(viewModel: OBDViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    var moreSubScreen by remember { mutableStateOf("menu") }
    val context = LocalContext.current

    var hasBluetoothPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasBluetoothPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            results[Manifest.permission.BLUETOOTH_SCAN] == true &&
            results[Manifest.permission.BLUETOOTH_CONNECT] == true
        } else {
            true
        }
        hasLocationPermission = results[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    val launchPermissionRequest = {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        try {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(Unit) {
        // Safe brief delay to let the UI settle before showing permission requests
        kotlinx.coroutines.delay(500)
        launchPermissionRequest()
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF11141A))
                    .border(width = 0.5.dp, color = Color(0xFF1E2530))
                    .navigationBarsPadding()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    NavigationTabItem("Dashboard", Icons.Filled.PlayArrow),
                    NavigationTabItem("Live Data", Icons.Filled.Tune),
                    NavigationTabItem("DTC", Icons.Filled.Warning),
                    NavigationTabItem("Graph", Icons.Filled.ShowChart),
                    NavigationTabItem("More", Icons.Filled.MoreHoriz)
                )

                tabs.forEachIndexed { idx, tab ->
                    val isSelected = selectedTab == idx
                    val contentColor = if (isSelected) Color(0xFF00E5FF) else Color(0xFF6B7280)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedTab = idx
                                if (idx == 4) {
                                    moreSubScreen = "menu"
                                }
                            }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = contentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.title,
                                fontSize = 8.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                color = contentColor,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Small horizontal indicator bar at the bottom
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(2.dp)
                                    .background(
                                        if (isSelected) Color(0xFF00E5FF) else Color.Transparent,
                                        shape = RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dynamic informative warning banner if permissions are missing
            if (!hasBluetoothPermission || !hasLocationPermission) {
                val missingText = buildString {
                    append("Permissions required for OBD2 communication:")
                    if (!hasBluetoothPermission) append(" Bluetooth")
                    if (!hasLocationPermission) {
                        if (!hasBluetoothPermission) append(" &")
                        append(" Location (GPS)")
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2C1619),
                        contentColor = Color(0xFFFF5252)
                    ),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { launchPermissionRequest() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = missingText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "ALLOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier
                                .background(Color(0xFF1A1C24), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTab) {
                    0 -> DashboardScreen(viewModel)
                    1 -> SensorGridScreen(viewModel)
                    2 -> DtcScreen(viewModel)
                    3 -> GraphScreen(viewModel)
                    4 -> {
                        when (moreSubScreen) {
                            "menu" -> MoreScreen(
                                viewModel = viewModel,
                                onNavigateToLogs = { moreSubScreen = "logs" },
                                onNavigateToSettings = { moreSubScreen = "settings" }
                            )
                            "logs" -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    SubScreenHeader(title = "SAVED LOGS", onBack = { moreSubScreen = "menu" })
                                    LogsScreen(viewModel)
                                }
                            }
                            "settings" -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    SubScreenHeader(title = "APP SETTINGS", onBack = { moreSubScreen = "menu" })
                                    SettingsScreen(viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF11141A))
            .border(width = 0.5.dp, color = Color(0xFF1E2530))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.clickable { onBack() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "BACK (MORE TOOLS)",
                color = Color(0xFF00E5FF),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

data class NavigationTabItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
