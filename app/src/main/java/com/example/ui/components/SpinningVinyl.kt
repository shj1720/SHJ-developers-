package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.Track

@Composable
fun SpinningVinyl(
    track: Track,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    accentColor: Color = Color(0xFF00F5D4)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_angle"
    )

    val currentRotation = if (isPlaying) rotation else 0f
    val context = LocalContext.current

    val imageResId = remember(track.albumArtDrawableName) {
        val name = track.albumArtDrawableName ?: "art_vinyl_disc"
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (id != 0) id else context.resources.getIdentifier("art_vinyl_disc", "drawable", context.packageName)
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(16.dp, CircleShape, spotColor = accentColor)
            .clip(CircleShape)
            .background(Color(0xFF0A0A0E))
            .border(3.dp, accentColor.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Vinyl body with subtle groove rings
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(currentRotation),
            contentAlignment = Alignment.Center
        ) {
            if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = "Vinyl Record",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fallback stylized vinyl
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF22222A), Color(0xFF0A0A0E), Color(0xFF14141A))
                            )
                        )
                )
            }

            // Center spindle hole
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A0A0E))
                    .border(2.dp, Color(0xFF444455), CircleShape)
            )
        }
    }
}
