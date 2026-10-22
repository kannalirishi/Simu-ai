package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.AudioWaveVisualizer
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.QuickCommandChips
import com.example.ui.theme.SimuCardBorder
import com.example.ui.theme.SimuCrimson
import com.example.ui.theme.SimuCyan
import com.example.ui.theme.SimuDeepSpace
import com.example.ui.theme.SimuSurfaceDark
import com.example.ui.theme.SimuSurfaceElevated
import com.example.ui.theme.SimuTextMuted
import com.example.ui.theme.SimuTextPrimary
import com.example.ui.theme.SimuTextSecondary
import com.example.ui.theme.SimuViolet
import com.example.ui.viewmodel.SimuViewModel

@Composable
fun ChatScreen(
    viewModel: SimuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val conversations by viewModel.conversations.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val rmsAmplitude by viewModel.voiceManager.rmsAmplitude.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    val listState = rememberLazyListState()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            viewModel.toggleVoiceInput(context, true)
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(conversations.size, isProcessing) {
        if (conversations.isNotEmpty()) {
            listState.animateScrollToItem(conversations.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SimuDeepSpace)
    ) {
        // Active Status Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SimuSurfaceDark)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (settings.chatMode == "online") SimuCyan else SimuViolet)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MODE: ${settings.chatMode.uppercase()} | ${settings.geminiModel}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SimuTextSecondary
                    )
                }

                if (isSpeaking) {
                    IconButton(
                        onClick = { viewModel.stopSpeaking() },
                        modifier = Modifier.size(24.dp).testTag("stop_speaking_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Speech",
                            tint = SimuCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Animated Audio Waveform when voice is active
        AudioWaveVisualizer(
            isListening = isListening,
            isSpeaking = isSpeaking,
            rmsAmplitude = rmsAmplitude
        )

        // Conversation history list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            items(conversations, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onSpeak = { text -> viewModel.speak(text) }
                )
            }

            if (isProcessing) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SimuCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = SimuCyan,
                                strokeWidth = 2.dp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SIMU is synthesizing response for ${settings.userTitle}...",
                            color = SimuTextSecondary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Quick command chips
        QuickCommandChips(
            onCommandClick = { cmd ->
                viewModel.submitQuery(context, cmd)
            }
        )

        // Input Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SimuSurfaceDark)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    placeholder = {
                        Text(
                            text = if (isListening) "Listening to you, ${settings.userTitle}..." else "Command SIMU or ask anything...",
                            color = SimuTextMuted,
                            fontSize = 14.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SimuCyan,
                        unfocusedBorderColor = SimuCardBorder,
                        focusedTextColor = SimuTextPrimary,
                        unfocusedTextColor = SimuTextPrimary,
                        focusedContainerColor = SimuSurfaceElevated,
                        unfocusedContainerColor = SimuSurfaceElevated
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Voice Mic Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) SimuCyan else SimuSurfaceElevated
                        )
                        .border(
                            1.dp,
                            if (isListening) SimuCyan else SimuCardBorder,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (!hasAudioPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.toggleVoiceInput(context, true)
                            }
                        },
                        modifier = Modifier.size(48.dp).testTag("mic_input_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop Listening" else "Voice Input",
                            tint = if (isListening) Color(0xFF00363D) else SimuCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) SimuViolet else SimuSurfaceElevated)
                        .border(1.dp, if (inputText.isNotBlank()) SimuViolet else SimuCardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.submitQuery(context)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isProcessing,
                        modifier = Modifier.size(48.dp).testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Command",
                            tint = if (inputText.isNotBlank()) Color.White else SimuTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
