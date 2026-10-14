package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.audio.VisualizerFrame

@Composable
fun AudioVisualizer(
    frame: VisualizerFrame,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    barCount: Int = 24
) {
    val animatedAmps = remember(barCount) {
        List(barCount) { Animatable(0.1f) }
    }

    LaunchedEffect(frame) {
        val step = (frame.frequencies.size.toFloat() / barCount).coerceAtLeast(1f)
        for (i in 0 until barCount) {
            val freqIndex = (i * step).toInt().coerceIn(0, frame.frequencies.size - 1)
            val target = frame.frequencies[freqIndex].coerceIn(0.08f, 1.0f)
            animatedAmps[i].animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
            )
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barSpacing = 4.dp.toPx()
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = ((totalWidth - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())

        val brush = Brush.verticalGradient(
            colors = listOf(secondaryColor, primaryColor),
            startY = 0f,
            endY = canvasHeight
        )

        for (i in 0 until barCount) {
            val amp = animatedAmps[i].value
            val barHeight = (canvasHeight * amp).coerceIn(4.dp.toPx(), canvasHeight)
            val left = i * (barWidth + barSpacing)
            val top = canvasHeight - barHeight

            drawRoundRect(
                brush = brush,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
