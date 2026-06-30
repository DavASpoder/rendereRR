package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ConsoleLog
import com.example.data.RendererSetting
import com.example.data.RegistrationProfile
import com.example.ui.RendererViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: RendererViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val isRegistered by viewModel.isRegistered.collectAsState()

    var currentTab by remember { mutableStateOf("engine") } // engine, shaders, console
    var showDiagnosticsDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        containerColor = DarkBg,
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            HeaderSection(
                onSettingsClick = { showDiagnosticsDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    "engine" -> {
                        EngineTabScreen(
                            settings = settings,
                            isRegistered = isRegistered,
                            onToggleSetting = { key, value -> viewModel.updateSetting(key, value) },
                            onRegisterClick = { viewModel.registerWithLauncher() }
                        )
                    }
                    "shaders" -> {
                        ShadersTabScreen(
                            profiles = profiles,
                            onProfileSelected = { viewModel.selectProfile(it) },
                            onClearCacheClick = { viewModel.clearShaderCache() }
                        )
                    }
                    "console" -> {
                        ConsoleTabScreen(
                            logs = logs,
                            onRunDiagnostics = { viewModel.runDiagnostics() },
                            onClearLogs = { viewModel.clearLogs() }
                        )
                    }
                }
            }
        }
    }

    if (showDiagnosticsDialog) {
        AlertDialog(
            onDismissRequest = { showDiagnosticsDialog = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Zink Mesa Info",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "This application serves as an external translation plugin for Zalith Launcher.",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Features:\n" +
                                "• Desktop OpenGL 4.6 to Vulkan 1.1 translation via Mesa Zink.\n" +
                                "• Aggressive mobile FP16 precision overrides for Mali Valhall GPUs.\n" +
                                "• Direct memory streaming structures for Sodium mod engine.\n" +
                                "• Standard Broadcast Handshake for automated discovery.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDiagnosticsDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = AccentBlue)
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun HeaderSection(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ZALITH ECOSYSTEM",
                style = MaterialTheme.typography.labelSmall,
                color = AccentBlue,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Renderer Plugin",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(22.dp))
                .clickable { onSettingsClick() }
                .testTag("settings_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Renderer settings",
                tint = AccentBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EngineTabScreen(
    settings: List<RendererSetting>,
    isRegistered: Boolean,
    onToggleSetting: (String, Boolean) -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // System Target Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "System Target",
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "SYSTEM TARGET",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SOC PLATFORM",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Unisoc Tiger T616",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MEMORY POOL",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "4GB LPDDR4X",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column {
                        Text(
                            text = "GRAPHICS ARCHITECTURE",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mali-G57 MP1 (Valhall)",
                            fontSize = 14.sp,
                            color = AccentBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Driver Parameters Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Driver Parameters",
                            tint = InfoBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "DRIVER PARAMETERS",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Toggles
                    ParameterToggleRow(
                        title = "Zink / GL4ES Wrapper",
                        subtitle = "OpenGL 4.6 to Vulkan Translation",
                        checked = settings.find { it.key == "zink_enabled" }?.value ?: true,
                        onCheckedChange = { onToggleSetting("zink_enabled", it) },
                        tag = "toggle_zink"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ParameterToggleRow(
                        title = "Force V-Sync Off",
                        subtitle = "vblank_mode=0 injection",
                        checked = settings.find { it.key == "vsync_disabled" }?.value ?: true,
                        onCheckedChange = { onToggleSetting("vsync_disabled", it) },
                        tag = "toggle_vsync"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ParameterToggleRow(
                        title = "FP16 Shader Scaling",
                        subtitle = "Optimize for narrow Mali bandwidth",
                        checked = settings.find { it.key == "fp16_enabled" }?.value ?: true,
                        onCheckedChange = { onToggleSetting("fp16_enabled", it) },
                        tag = "toggle_fp16"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = DarkBorder, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SODIUM INTEROP",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "READY",
                            fontSize = 11.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "IRIS SHADER COMPAT",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "OPTIMIZED",
                            fontSize = 11.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Action Button
        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("register_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = AccentText
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (isRegistered) "REGISTERED WITH LAUNCHER" else "REGISTER WITH LAUNCHER",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
fun ParameterToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SuccessGreen,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = DarkBg
            )
        )
    }
}

@Composable
fun ShadersTabScreen(
    profiles: List<RegistrationProfile>,
    onProfileSelected: (String) -> Unit,
    onClearCacheClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Manager Header
            Text(
                text = "GPU PROFILES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBlue,
                letterSpacing = 1.sp
            )

            profiles.forEach { profile ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (profile.active) AccentBlue else DarkBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onProfileSelected(profile.profileId) }
                        .testTag("profile_${profile.profileId}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (profile.active) Color(0x15, 0xD1, 0xE1, 0xFF) else DarkSurface
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = profile.profileName,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                if (profile.active) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SuccessGreen)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SoC: ${profile.targetSoC} • GPU: ${profile.targetGpu}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Allocation: ${profile.ramAllocated}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        RadioButton(
                            selected = profile.active,
                            onClick = { onProfileSelected(profile.profileId) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AccentBlue,
                                unselectedColor = DarkBorder
                            )
                        )
                    }
                }
            }

            // Benchmark Targets Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ESTIMATED PERFORMANCE TARGETS",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Vanilla Minecraft 1.20+",
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Optimized rendering loop",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "80+ FPS",
                            fontSize = 18.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Shader Target (Iris)",
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "FP16 half-precision scale",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "30+ FPS",
                            fontSize = 18.sp,
                            color = InfoBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Cache Cleaner Action
        Button(
            onClick = onClearCacheClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("clear_cache_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkSurface,
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Clear Cache",
                    tint = WarningOrange,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "CLEAR SHADER CACHE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.5.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ConsoleTabScreen(
    logs: List<ConsoleLog>,
    onRunDiagnostics: () -> Unit,
    onClearLogs: () -> Unit
) {
    val listState = rememberLazyListState()

    // Autoscroll to the bottom when logs list updates
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRANSLATION ENGINE LOGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue,
                    letterSpacing = 1.sp
                )

                TextButton(
                    onClick = onClearLogs,
                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Scrollable Console Window
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF06080A))
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .padding(12.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (logs.isEmpty()) {
                        item {
                            Text(
                                text = "No driver logs recorded.",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp)
                            )
                        }
                    } else {
                        items(logs) { log ->
                            ConsoleLogLine(log = log)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Diagnostic run action button
        Button(
            onClick = onRunDiagnostics,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("run_diagnostics_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = AccentText
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Diagnostics",
                    tint = AccentText,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "RUN DIAGNOSTICS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

@Composable
fun ConsoleLogLine(log: ConsoleLog) {
    val color = when (log.level) {
        "SUCCESS" -> SuccessGreen
        "WARN" -> WarningOrange
        "ERROR" -> Color.Red
        else -> TextPrimary
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "[${log.tag}] ",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentBlue,
            maxLines = 1
        )
        Text(
            text = log.message,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = color,
            lineHeight = 15.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BottomNavBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = DarkSurface,
        border = BorderStroke(width = 0.5.dp, color = DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Engine",
                icon = Icons.Default.Build,
                selected = currentTab == "engine",
                onClick = { onTabSelected("engine") },
                tag = "tab_engine"
            )

            BottomNavItem(
                label = "Shaders",
                icon = Icons.Default.Star,
                selected = currentTab == "shaders",
                onClick = { onTabSelected("shaders") },
                tag = "tab_shaders"
            )

            BottomNavItem(
                label = "Console",
                icon = Icons.Default.List,
                selected = currentTab == "console",
                onClick = { onTabSelected("console") },
                tag = "tab_console"
            )
        }
    }
}

@Composable
fun BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) AccentBlue else TextSecondary,
            modifier = Modifier
                .size(24.dp)
                .padding(bottom = 2.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) Color.White else TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
