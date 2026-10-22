package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SimuCardBorder
import com.example.ui.theme.SimuCyan
import com.example.ui.theme.SimuDeepSpace
import com.example.ui.theme.SimuSurfaceDark
import com.example.ui.theme.SimuSurfaceElevated
import com.example.ui.theme.SimuTextMuted
import com.example.ui.theme.SimuTextPrimary
import com.example.ui.theme.SimuTextSecondary
import com.example.ui.theme.SimuViolet
import com.example.ui.viewmodel.SimuTab
import com.example.ui.viewmodel.SimuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SimuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    // Handle back button to return to Console if in secondary tabs
    BackHandler(enabled = currentTab != SimuTab.CHAT) {
        viewModel.selectTab(SimuTab.CHAT)
    }

    // Collect Toast events
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SimuDeepSpace,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SimuCyan.copy(alpha = 0.15f))
                                .border(1.dp, SimuCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "SIMU AI Core",
                                tint = SimuCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SIMU AI",
                                    color = SimuTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SimuCyan.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = settings.userTitle,
                                        color = SimuCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Text(
                                text = "PERSONAL AI CONSOLE",
                                color = SimuTextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                actions = {
                    // Quick Auto-speak mute/unmute toggle in top bar
                    IconButton(
                        onClick = {
                            val newSpeak = !settings.autoSpeak
                            viewModel.preferences.updateSettings(autoSpeak = newSpeak)
                            if (!newSpeak) viewModel.stopSpeaking()
                        },
                        modifier = Modifier.testTag("topbar_speech_toggle")
                    ) {
                        Icon(
                            imageVector = if (settings.autoSpeak) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = if (settings.autoSpeak) "Voice Output Enabled" else "Voice Output Muted",
                            tint = if (settings.autoSpeak) SimuCyan else SimuTextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SimuSurfaceDark,
                    titleContentColor = SimuTextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SimuSurfaceDark,
                modifier = Modifier
                    .border(width = 1.dp, color = SimuCardBorder)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                val navItems = listOf(
                    Triple(SimuTab.CHAT, Icons.AutoMirrored.Filled.Chat, "Console"),
                    Triple(SimuTab.MEMORY, Icons.Default.Psychology, "Memory"),
                    Triple(SimuTab.NOTES, Icons.Default.Description, "Notes"),
                    Triple(SimuTab.APPS, Icons.Default.RocketLaunch, "Apps"),
                    Triple(SimuTab.SETTINGS, Icons.Default.Settings, "Settings")
                )

                navItems.forEach { (tab, icon, label) ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SimuCyan,
                            selectedTextColor = SimuCyan,
                            indicatorColor = SimuCyan.copy(alpha = 0.2f),
                            unselectedIconColor = SimuTextMuted,
                            unselectedTextColor = SimuTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                SimuTab.CHAT -> ChatScreen(viewModel = viewModel)
                SimuTab.MEMORY -> MemoryScreen(viewModel = viewModel)
                SimuTab.NOTES -> NotesScreen(viewModel = viewModel)
                SimuTab.APPS -> AppsScreen(viewModel = viewModel)
                SimuTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
