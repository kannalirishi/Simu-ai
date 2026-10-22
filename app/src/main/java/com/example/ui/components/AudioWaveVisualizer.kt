package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SimuCyan
import com.example.ui.theme.SimuDeepSpace
import com.example.ui.theme.SimuSurfaceElevated
import com.example.ui.theme.SimuViolet

@Composable
fun AudioWaveVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    rmsAmplitude: Float,
    modifier: Modifier = Modifier
) {
    if (!isListening && !isSpeaking) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val activeColor = if (isListening) SimuCyan else SimuViolet
    val statusText = if (isListening) "LISTENING TO VOICE..." else "SPEAKING..."

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SimuSurfaceElevated)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Animated glowing indicator dot
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(activeColor.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size((8 * pulseScale).dp)
                            .clip(CircleShape)
                            .background(activeColor)
                    )
                }

                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = activeColor
                )
            }

            // Real-time audio waveform canvas
            Canvas(
                modifier = Modifier
                    .size(width = 100.dp, height = 24.dp)
            ) {
                val barWidth = 4.dp.toPx()
                val spacing = 3.dp.toPx()
                val totalBars = 12
                val midY = size.height / 2f

                for (i in 0 until totalBars) {
                    val factor = kotlin.math.sin((i.toDouble() / totalBars) * Math.PI).toFloat()
                    val dynamicAmp = (rmsAmplitude * 0.8f + (pulseScale - 0.85f) * 0.4f).coerceIn(0.15f, 1.0f)
                    val barHeight = (size.height * 0.9f * factor * dynamicAmp).coerceAtLeast(4f)

                    val x = i * (barWidth + spacing)
                    drawLine(
                        color = activeColor.copy(alpha = 0.5f + factor * 0.5f),
                        start = Offset(x, midY - barHeight / 2),
                        end = Offset(x, midY + barHeight / 2),
                        strokeWidth = barWidth
                    )
                }
            }
        }
    }
}
