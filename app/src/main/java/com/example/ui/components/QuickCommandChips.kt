package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SimuCardBorder
import com.example.ui.theme.SimuCyan
import com.example.ui.theme.SimuSurfaceElevated
import com.example.ui.theme.SimuTextPrimary

@Composable
fun QuickCommandChips(
    onCommandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickCommands = listOf(
        "Open YouTube",
        "Open WhatsApp",
        "Open Camera",
        "Open Chrome",
        "Status",
        "Memory",
        "Notes",
        "Help"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        quickCommands.forEach { cmd ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SimuSurfaceElevated)
                    .border(1.dp, SimuCardBorder, RoundedCornerShape(12.dp))
                    .clickable { onCommandClick(cmd) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("quick_chip_${cmd.lowercase().replace(" ", "_")}")
            ) {
                Text(
                    text = cmd,
                    color = SimuCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
