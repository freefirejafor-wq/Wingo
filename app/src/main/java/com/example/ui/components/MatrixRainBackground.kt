package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardBgDark
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.VipGold
import kotlinx.coroutines.delay

@Composable
fun MatrixRainBackground(
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var tick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect
        while (true) {
            delay(90L)
            tick++
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "vipAura")
    val auraShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraShift"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 10f || h <= 10f) return@Canvas

        // Luxury Radial Ambient Glow (Top Gold & Center Emerald)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    VipGold.copy(alpha = 0.08f + auraShift * 0.03f),
                    Color.Transparent
                ),
                center = Offset(w * 0.25f, h * 0.08f),
                radius = w * 0.75f
            ),
            radius = w * 0.75f,
            center = Offset(w * 0.25f, h * 0.08f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonGreen.copy(alpha = 0.06f + (1f - auraShift) * 0.03f),
                    Color.Transparent
                ),
                center = Offset(w * 0.8f, h * 0.35f),
                radius = w * 0.7f
            ),
            radius = w * 0.7f,
            center = Offset(w * 0.8f, h * 0.35f)
        )

        if (enabled) {
            val colWidth = 32.dp.toPx().coerceAtLeast(22f)
            val cols = (w / colWidth).toInt().coerceIn(8, 30)
            val rowHeight = 20.dp.toPx().coerceAtLeast(15f)
            val maxRows = (h / rowHeight).toInt().coerceAtLeast(15)

            for (c in 0 until cols) {
                val speed = (c % 2) + 1
                val headRow = ((tick * speed + c * 5) % (maxRows + 10)).toInt()
                val x = c * colWidth + (colWidth * 0.4f)

                for (trail in 0..4) {
                    val r = headRow - trail
                    if (r in 0 until maxRows) {
                        val y = r * rowHeight
                        val alpha = when (trail) {
                            0 -> 0.24f
                            1 -> 0.14f
                            2 -> 0.08f
                            else -> 0.03f
                        }
                        val barColor = when {
                            trail == 0 && c % 2 == 0 -> VipGold.copy(alpha = alpha)
                            trail == 0 -> NeonGreen.copy(alpha = alpha)
                            else -> CyberCyan.copy(alpha = alpha)
                        }
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(2.5.dp.toPx(), (rowHeight * 0.55f)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = VipGold.copy(alpha = 0.28f),
    paddingDp: Dp = 10.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CyberCardBg.copy(alpha = 0.95f),
                        CyberCardBgDark.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.14f)
                    )
                ),
                shape = shape
            )
            .padding(paddingDp),
        content = content
    )
}

@Composable
fun CircularCountdownTimer(
    remainingSeconds: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 42.dp
) {
    val ringColor = when {
        remainingSeconds <= 5 -> NeonRed
        remainingSeconds <= 10 -> NeonAmber
        else -> NeonGreen
    }

    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = if (sizeDp < 50.dp) 3.5.dp.toPx() else 6.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            drawArc(
                color = Color(0xFF1A243B),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "${remainingSeconds}s",
            fontFamily = OrbitronFamily,
            fontWeight = FontWeight.Black,
            fontSize = if (sizeDp < 50.dp) 11.sp else 18.sp,
            color = ringColor
        )
    }
}
