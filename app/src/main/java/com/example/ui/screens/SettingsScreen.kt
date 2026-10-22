package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.prefs.SimuPreferences
import com.example.ui.theme.SimuAmber
import com.example.ui.theme.SimuCardBorder
import com.example.ui.theme.SimuCrimson
import com.example.ui.theme.SimuCyan
import com.example.ui.theme.SimuDeepSpace
import com.example.ui.theme.SimuEmerald
import com.example.ui.theme.SimuSurfaceElevated
import com.example.ui.theme.SimuTextMuted
import com.example.ui.theme.SimuTextPrimary
import com.example.ui.theme.SimuTextSecondary
import com.example.ui.theme.SimuViolet
import com.example.ui.viewmodel.SimuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SimuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settingsState.collectAsState()

    var apiKeyInput by remember(settings.geminiApiKey) { mutableStateOf(settings.geminiApiKey) }
    var showApiKey by remember { mutableStateOf(false) }

    var selectedModel by remember(settings.geminiModel) { mutableStateOf(settings.geminiModel) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    var localUrlInput by remember(settings.localChatUrl) { mutableStateOf(settings.localChatUrl) }
    var userTitleInput by remember(settings.userTitle) { mutableStateOf(settings.userTitle) }

    var ttsPitch by remember(settings.ttsPitch) { mutableFloatStateOf(settings.ttsPitch) }
    var ttsSpeed by remember(settings.ttsSpeed) { mutableFloatStateOf(settings.ttsSpeed) }

    val effectiveApiKey = viewModel.preferences.getEffectiveApiKey()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SimuDeepSpace)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Header
        Text(
            text = "SYSTEM CONFIGURATION",
            color = SimuCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
        )

        // 1. Gemini API Key Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SimuCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SimuSurfaceElevated),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = SimuCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini API Key",
                        color = SimuTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (effectiveApiKey.isNotBlank()) "Status: Active API Key Configured" else "Status: No API key detected. Please add one below.",
                    color = if (effectiveApiKey.isNotBlank()) SimuEmerald else SimuAmber,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input"),
                    placeholder = { Text("Paste Gemini API Key (AIza...)", color = SimuTextMuted) },
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle visibility",
                                tint = SimuTextSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SimuCyan,
                        unfocusedBorderColor = SimuCardBorder,
                        focusedTextColor = SimuTextPrimary,
                        unfocusedTextColor = SimuTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (apiKeyInput.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                apiKeyInput = ""
                                viewModel.preferences.updateSettings(geminiApiKey = "")
                                Toast.makeText(context, "API Key cleared", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("clear_api_key_button")
                        ) {
                            Text("Clear", color = SimuCrimson)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = {
                            viewModel.preferences.updateSettings(geminiApiKey = apiKeyInput.trim())
                            Toast.makeText(context, "Gemini API Key saved, Sir.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SimuCyan),
                        modifier = Modifier.testTag("save_api_key_button")
                    ) {
                        Text("Save Key", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Gemini Model Version Selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SimuCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SimuSurfaceElevated),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = SimuViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini Model Version",
                        color = SimuTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select model version for online inference:",
                    color = SimuTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = modelDropdownExpanded,
                    onExpandedChange = { modelDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedModel,
                        onValueChange = { selectedModel = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("model_version_selector"),
                        readOnly = false,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SimuViolet,
                            unfocusedBorderColor = SimuCardBorder,
                            focusedTextColor = SimuTextPrimary,
                            unfocusedTextColor = SimuTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = modelDropdownExpanded,
                        onDismissRequest = { modelDropdownExpanded = false },
                        containerColor = SimuSurfaceElevated
                    ) {
                        SimuPreferences.AVAILABLE_MODELS.forEach { (modelId, desc) ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = modelId, color = SimuTextPrimary, fontWeight = FontWeight.Bold)
                                        Text(text = desc, color = SimuTextSecondary, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    if (modelId != "custom") {
                                        selectedModel = modelId
                                        viewModel.preferences.updateSettings(geminiModel = modelId)
                                    }
                                    modelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = {
                            viewModel.preferences.updateSettings(geminiModel = selectedModel.trim())
                            Toast.makeText(context, "Model set to $selectedModel", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SimuViolet),
                        modifier = Modifier.testTag("save_model_button")
                    ) {
                        Text("Apply Model", color = Color.White)
                    }
                }
            }
        }

        // 3. Execution Mode (Auto / Online / Offline)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SimuCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SimuSurfaceElevated),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AI Execution Mode",
                    color = SimuTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Auto tries Gemini first, then falls back to local server / offline memory.",
                    color = SimuTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf("auto" to "Auto", "online" to "Online", "local" to "Offline")
                    modes.forEach { (modeKey, modeTitle) ->
                        val isSelected = settings.chatMode == modeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SimuCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .border(1.dp, if (isSelected) SimuCyan else SimuCardBorder, RoundedCornerShape(12.dp))
                                .clickable { viewModel.preferences.updateSettings(chatMode = modeKey) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = modeTitle,
                                color = if (isSelected) SimuCyan else SimuTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Local Server URL
                Text(
                    text = "Local llama.cpp Server URL:",
                    color = SimuTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = localUrlInput,
                    onValueChange = { localUrlInput = it },
                    modifier = Modifier.fillMaxWidth().testTag("local_url_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SimuCyan,
                        unfocusedBorderColor = SimuCardBorder,
                        focusedTextColor = SimuTextPrimary,
                        unfocusedTextColor = SimuTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = {
                            viewModel.preferences.updateSettings(localChatUrl = localUrlInput.trim())
                            Toast.makeText(context, "Local endpoint saved", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SimuCyan),
                        modifier = Modifier.testTag("save_local_url_button")
                    ) {
                        Text("Save URL", color = Color(0xFF00363D))
                    }
                }
            }
        }

        // 4. Personalization & Title ("calls me sir and etc")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SimuCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SimuSurfaceElevated),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Personalization & Salutation",
                    color = SimuTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "How SIMU addresses you in conversations and system responses:",
                    color = SimuTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = userTitleInput,
                    onValueChange = { userTitleInput = it },
                    modifier = Modifier.fillMaxWidth().testTag("user_title_input"),
                    placeholder = { Text("e.g. Sir, Boss, Captain") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SimuCyan,
                        unfocusedBorderColor = SimuCardBorder,
                        focusedTextColor = SimuTextPrimary,
                        unfocusedTextColor = SimuTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = {
                            val title = userTitleInput.trim().ifBlank { "Sir" }
                            viewModel.preferences.updateSettings(userTitle = title)
                            Toast.makeText(context, "SIMU will address you as $title", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SimuCyan),
                        modifier = Modifier.testTag("save_title_button")
                    ) {
                        Text("Save Title", color = Color(0xFF00363D))
                    }
                }
            }
        }

        // 5. Voice & Speech Synthesis Controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SimuCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SimuSurfaceElevated),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = SimuCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Auto-Speak Responses",
                            color = SimuTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Switch(
                        checked = settings.autoSpeak,
                        onCheckedChange = { viewModel.preferences.updateSettings(autoSpeak = it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SimuCyan,
                            checkedTrackColor = SimuCyan.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("auto_speak_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pitch Slider
                Text(
                    text = "Voice Pitch: ${String.format("%.1f", ttsPitch)}x",
                    color = SimuTextSecondary,
                    fontSize = 13.sp
                )
                Slider(
                    value = ttsPitch,
                    onValueChange = {
                        ttsPitch = it
                        viewModel.preferences.updateSettings(ttsPitch = it)
                    },
                    valueRange = 0.5f..1.8f,
                    colors = SliderDefaults.colors(
                        thumbColor = SimuCyan,
                        activeTrackColor = SimuCyan
                    )
                )

                // Speed Slider
                Text(
                    text = "Speech Rate: ${String.format("%.1f", ttsSpeed)}x",
                    color = SimuTextSecondary,
                    fontSize = 13.sp
                )
                Slider(
                    value = ttsSpeed,
                    onValueChange = {
                        ttsSpeed = it
                        viewModel.preferences.updateSettings(ttsSpeed = it)
                    },
                    valueRange = 0.5f..1.8f,
                    colors = SliderDefaults.colors(
                        thumbColor = SimuViolet,
                        activeTrackColor = SimuViolet
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.speak("Voice synthesis calibrated and online. Ready for your commands, ${settings.userTitle}.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SimuSurfaceElevated),
                    modifier = Modifier.fillMaxWidth().border(1.dp, SimuCyan, RoundedCornerShape(12.dp)).testTag("test_voice_button")
                ) {
                    Text("Test Voice Output", color = SimuCyan, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 6. Data Management
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SimuCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SimuSurfaceElevated),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Database & Maintenance",
                    color = SimuTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SQLite Database: simu_memory.db\nFacts and Notes remain preserved.",
                    color = SimuTextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.clearHistory()
                        Toast.makeText(context, "Conversation history cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SimuCrimson),
                    modifier = Modifier.fillMaxWidth().testTag("clear_history_button")
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Chat History")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
