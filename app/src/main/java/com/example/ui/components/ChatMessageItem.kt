package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.SimuConversation
import com.example.ui.theme.SimuAiBubble
import com.example.ui.theme.SimuAmber
import com.example.ui.theme.SimuCardBorder
import com.example.ui.theme.SimuCyan
import com.example.ui.theme.SimuEmerald
import com.example.ui.theme.SimuTextMuted
import com.example.ui.theme.SimuTextPrimary
import com.example.ui.theme.SimuTextSecondary
import com.example.ui.theme.SimuUserBubble
import com.example.ui.theme.SimuViolet

@Composable
fun ChatMessageItem(
    message: SimuConversation,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .testTag(if (isUser) "user_message_item" else "assistant_message_item"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Assistant Hologram Avatar
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
                    contentDescription = "SIMU AI",
                    tint = SimuCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Bubble Box
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isUser) SimuUserBubble else SimuAiBubble)
                    .border(
                        width = 1.dp,
                        color = if (isUser) SimuViolet.copy(alpha = 0.4f) else SimuCardBorder,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    if (!isUser && message.source.isNotBlank()) {
                        SourceBadge(source = message.source)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = message.message,
                        color = SimuTextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )
                }
            }

            // Footer info and action buttons
            Row(
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.createdAt.takeLast(8), // Just HH:mm:ss
                    fontSize = 11.sp,
                    color = SimuTextMuted,
                    fontFamily = FontFamily.Monospace
                )

                if (!isUser) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { onSpeak(message.message) },
                        modifier = Modifier.size(32.dp).testTag("speak_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak Message",
                            tint = SimuCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("SIMU AI", message.message))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp).testTag("copy_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Message",
                            tint = SimuTextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SimuViolet.copy(alpha = 0.2f))
                    .border(1.dp, SimuViolet, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = SimuViolet,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SourceBadge(source: String) {
    val (badgeBg, badgeFg, label) = when (source) {
        "ONLINE GEMINI" -> Triple(SimuCyan.copy(alpha = 0.15f), SimuCyan, "⚡ ONLINE GEMINI")
        "OFFLINE LOCAL" -> Triple(SimuViolet.copy(alpha = 0.15f), SimuViolet, "🖥️ OFFLINE QWEN")
        "APP LAUNCHER" -> Triple(SimuEmerald.copy(alpha = 0.15f), SimuEmerald, "🚀 APP LAUNCHED")
        "SYSTEM MEMORY" -> Triple(SimuAmber.copy(alpha = 0.15f), SimuAmber, "🧠 SQLITE MEMORY")
        else -> Triple(Color(0xFF2A3656), SimuTextSecondary, source)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeBg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = badgeFg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}
