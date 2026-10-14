package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import com.example.ui.components.DownloadStatusButton
import com.example.ui.components.OfflineNoticeDialog
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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ReverbRoom
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.CassetteTape
import com.example.ui.components.SpinningVinyl
import com.example.viewmodel.MusicPlayerViewModel
import com.example.viewmodel.NavigationTab
import com.example.viewmodel.RepeatMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val track by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val visualizerFrame by viewModel.visualizerFrame.collectAsState()
    val theme by viewModel.currentTheme.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val activePreset by viewModel.activePreset.collectAsState()
    val lofiSettings by viewModel.loFiSettings.collectAsState()
    val reverbRoom by viewModel.reverbRoom.collectAsState()
    val isEffectiveOffline by viewModel.isEffectiveOffline.collectAsState()
    val downloadingTrackIds by viewModel.downloadingTrackIds.collectAsState()
    val offlineNoticeMessage by viewModel.offlineNoticeMessage.collectAsState()

    var visualModeIsVinyl by remember { mutableStateOf(true) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(0f) }

    val currentProgress = if (isDraggingSlider) {
        dragProgress
    } else {
        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val elapsedSeconds = if (isDraggingSlider) (dragProgress * durationMs / 1000).toLong() else currentPositionMs / 1000
    val totalSeconds = durationMs / 1000
    val elapsedString = "%d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60)
    val totalString = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("player_screen"),
        color = theme.backgroundColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Header: Title & Switch visualizer style (Vinyl vs Cassette)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NOW PLAYING",
                                color = theme.primaryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            if (isEffectiveOffline) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2EC4B6).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CloudOff,
                                            contentDescription = null,
                                            tint = Color(0xFF2EC4B6),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Offline",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2EC4B6)
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = if (track.isDownloaded) "SHJ Lossless Offline • Local Cache" else "SHJ High-Res Audio",
                            color = if (track.isDownloaded) Color(0xFF2EC4B6) else theme.onSurfaceColor.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }

                    // Visualizer Mode Switch (Vinyl / Cassette)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(theme.surfaceColor)
                            .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (visualModeIsVinyl) theme.primaryColor.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { visualModeIsVinyl = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Vinyl",
                            fontSize = 11.sp,
                            fontWeight = if (visualModeIsVinyl) FontWeight.Bold else FontWeight.Normal,
                            color = if (visualModeIsVinyl) theme.primaryColor else theme.onSurfaceColor.copy(alpha = 0.7f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (!visualModeIsVinyl) theme.primaryColor.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { visualModeIsVinyl = false }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Cassette",
                            fontSize = 11.sp,
                            fontWeight = if (!visualModeIsVinyl) FontWeight.Bold else FontWeight.Normal,
                            color = if (!visualModeIsVinyl) theme.primaryColor else theme.onSurfaceColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Center Visualizer Animation (Vinyl or Cassette)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                if (visualModeIsVinyl) {
                    SpinningVinyl(
                        track = track,
                        isPlaying = isPlaying,
                        size = 240.dp,
                        accentColor = theme.primaryColor
                    )
                } else {
                    CassetteTape(
                        track = track,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxWidth(0.95f),
                        accentColor = theme.primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Track Title, Artist, and Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = theme.onSurfaceColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${track.artist} • ${track.album}",
                        color = theme.onSurfaceColor.copy(alpha = 0.75f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    DownloadStatusButton(
                        isDownloaded = track.isDownloaded,
                        downloadProgress = downloadingTrackIds[track.id],
                        theme = theme,
                        onDownloadClick = { viewModel.downloadTrack(track) },
                        onRemoveClick = { viewModel.deleteDownloadedTrack(track) },
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    IconButton(
                        onClick = { viewModel.toggleFavorite(track) },
                        modifier = Modifier.testTag("player_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (track.isFavorite) theme.secondaryColor else theme.onSurfaceColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Audio Visualizer Wave
            AudioVisualizer(
                frame = visualizerFrame,
                primaryColor = theme.primaryColor,
                secondaryColor = theme.secondaryColor,
                modifier = Modifier.fillMaxWidth(),
                height = 42.dp,
                barCount = 28
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scrubber Slider
            Slider(
                value = currentProgress,
                onValueChange = {
                    isDraggingSlider = true
                    dragProgress = it
                },
                onValueChangeFinished = {
                    isDraggingSlider = false
                    val targetMs = (dragProgress * durationMs).toLong()
                    viewModel.seekTo(targetMs)
                },
                colors = SliderDefaults.colors(
                    thumbColor = theme.primaryColor,
                    activeTrackColor = theme.primaryColor,
                    inactiveTrackColor = theme.surfaceVariantColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_scrub_slider")
            )

            // Elapsed and Remaining Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = elapsedString,
                    color = theme.onSurfaceColor.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = totalString,
                    color = theme.onSurfaceColor.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) theme.primaryColor else theme.onSurfaceColor.copy(alpha = 0.45f)
                    )
                }

                // Previous Button
                IconButton(
                    onClick = { viewModel.playPreviousTrack() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_previous_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = theme.onSurfaceColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play / Pause Main Glowing Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(18.dp, CircleShape, spotColor = theme.primaryColor)
                        .clip(CircleShape)
                        .background(theme.primaryColor)
                        .clickable { viewModel.togglePlayPause() }
                        .testTag("player_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color(0xFF0A0E17),
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Next Button
                IconButton(
                    onClick = { viewModel.playNextTrack() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = theme.onSurfaceColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Repeat Mode Button
                IconButton(
                    onClick = { viewModel.cycleRepeatMode() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = when (repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF) theme.primaryColor else theme.onSurfaceColor.copy(alpha = 0.45f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sound Effects & Profiles Shortcut Cards
            Text(
                text = "ACTIVE AUDIO ENHANCEMENTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = theme.primaryColor,
                letterSpacing = 1.2.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Equalizer Preset Chip
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.surfaceColor)
                        .border(1.dp, theme.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.setActiveTab(NavigationTab.EQUALIZER) }
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "EQ",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "EQUALIZER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryColor
                            )
                            Text(
                                text = activePreset.name.take(14),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.onSurfaceColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Lo-Fi Tuner Chip
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.surfaceColor)
                        .border(1.dp, theme.secondaryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.setActiveTab(NavigationTab.LOFI) }
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = "Lo-Fi",
                            tint = theme.secondaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "LO-FI TUNER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.secondaryColor
                            )
                            Text(
                                text = if (lofiSettings.isEnabled) lofiSettings.activeTone.displayName.take(14) else "Bypassed",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.onSurfaceColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reverb Room Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surfaceColor)
                    .clickable { viewModel.setActiveTab(NavigationTab.LOFI) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SurroundSound,
                            contentDescription = "Reverb",
                            tint = theme.accentGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reverb Space: ${reverbRoom.displayName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.onSurfaceColor
                        )
                    }

                    Text(
                        text = "Customize ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Offline Notice Dialog if user tries to trigger undownloaded track in offline mode
        offlineNoticeMessage?.let { notice ->
            OfflineNoticeDialog(
                message = notice,
                theme = theme,
                onDismiss = { viewModel.dismissOfflineNotice() },
                onDisableOfflineMode = {
                    viewModel.setOfflineModeForced(false)
                    viewModel.dismissOfflineNotice()
                }
            )
        }
    }
}
}

