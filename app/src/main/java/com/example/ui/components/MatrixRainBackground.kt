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
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFamily
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
            delay(85L)
            tick++
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanY"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 10f || h <= 10f) return@Canvas

        if (enabled) {
            val colWidth = 26.dp.toPx().coerceAtLeast(18f)
            val cols = (w / colWidth).toInt().coerceIn(8, 36)
            val rowHeight = 18.dp.toPx().coerceAtLeast(14f)
            val maxRows = (h / rowHeight).toInt().coerceAtLeast(15)

            for (c in 0 until cols) {
                val speed = (c % 3) + 1
                val headRow = ((tick * speed + c * 7) % (maxRows + 8)).toInt()
                val x = c * colWidth + (colWidth * 0.3f)

                for (trail in 0..5) {
                    val r = headRow - trail
                    if (r in 0 until maxRows) {
                        val y = r * rowHeight
                        val alpha = when (trail) {
                            0 -> 0.34f
                            1 -> 0.22f
                            2 -> 0.14f
                            3 -> 0.09f
                            else -> 0.04f
                        }
                        val barColor = if (trail == 0) NeonGreen.copy(alpha = alpha) else CyberCyan.copy(alpha = alpha)
                        // Draw digital matrix glyph segments using fast GPU primitives (zero text layout overhead!)
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(3.dp.toPx(), (rowHeight * 0.65f)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                    }
                }
            }
        }

        // Subtle CRT Scanline band
        val bandTop = (scanY * h)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    CyberCyan.copy(alpha = 0.06f),
                    Color.Transparent
                ),
                startY = (bandTop - 80f).coerceAtLeast(0f),
                endY = (bandTop + 80f).coerceAtMost(h)
            ),
            topLeft = Offset(0f, (bandTop - 80f).coerceAtLeast(0f)),
            size = Size(w, 160f)
        )
    }
}

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CyberBorder.copy(alpha = 0.35f),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CyberCardBg.copy(alpha = 0.92f),
                        Color(0xFF040D1A).copy(alpha = 0.94f)
                    )
                )
            )
            .border(width = 1.dp, color = borderColor, shape = shape)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun CircularCountdownTimer(
    remainingSeconds: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 94.dp
) {
    val ringColor = when {
        remainingSeconds <= 5 -> NeonRed
        remainingSeconds <= 10 -> NeonGreen
        else -> CyberCyan
    }

    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 7.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            drawArc(
                color = Color(0xFF102235),
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
            fontSize = 21.sp,
            color = ringColor
        )
    }
}
