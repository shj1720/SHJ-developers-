package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.StorageStats
import com.example.model.Playlist
import com.example.model.SHJTheme
import com.example.model.Track

/**
 * Visual download indicator / action button for songs and playlists
 */
@Composable
fun DownloadStatusButton(
    isDownloaded: Boolean,
    downloadProgress: Float?,
    theme: SHJTheme,
    onDownloadClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        when {
            downloadProgress != null -> {
                // Currently downloading
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(theme.surfaceColor),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.size(24.dp),
                        color = theme.primaryColor,
                        strokeWidth = 2.5.dp
                    )
                    Text(
                        text = "${(downloadProgress * 100).toInt()}%",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryColor
                    )
                }
            }
            isDownloaded -> {
                // Downloaded
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("downloaded_indicator_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Downloaded for offline playback",
                        tint = Color(0xFF2EC4B6),
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (showMenu) {
                    AlertDialog(
                        onDismissRequest = { showMenu = false },
                        containerColor = theme.surfaceColor,
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DownloadDone,
                                    contentDescription = null,
                                    tint = Color(0xFF2EC4B6)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Downloaded Music",
                                    color = theme.onSurfaceColor,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        text = {
                            Text(
                                text = "This track is stored locally in high quality and is available for offline listening anytime.",
                                color = theme.onSurfaceColor.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { showMenu = false }) {
                                Text("Keep Offline", color = theme.primaryColor)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showMenu = false
                                    onRemoveClick()
                                }
                            ) {
                                Text("Remove Download", color = Color(0xFFFF5376))
                            }
                        }
                    )
                }
            }
            else -> {
                // Not downloaded
                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("download_track_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download for offline listening",
                        tint = theme.onSurfaceColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Banner displaying Offline Mode status, toggle, and storage metrics
 */
@Composable
fun OfflineModeBanner(
    isOfflineModeForced: Boolean,
    isDeviceOnline: Boolean,
    isEffectiveOffline: Boolean,
    storageStats: StorageStats,
    theme: SHJTheme,
    onToggleOfflineMode: () -> Unit,
    onOpenStorageManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_mode_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEffectiveOffline) theme.primaryColor.copy(alpha = 0.12f) else theme.surfaceColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isEffectiveOffline) theme.primaryColor.copy(alpha = 0.5f) else theme.surfaceVariantColor.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isEffectiveOffline) theme.primaryColor.copy(alpha = 0.2f)
                                else theme.surfaceVariantColor
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEffectiveOffline) Icons.Default.CloudOff else Icons.Default.Wifi,
                            contentDescription = "Offline Mode Status",
                            tint = if (isEffectiveOffline) theme.primaryColor else Color(0xFF2EC4B6),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isEffectiveOffline) "OFFLINE MODE ACTIVE" else "ONLINE STREAMING",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isEffectiveOffline) theme.primaryColor else theme.onSurfaceColor
                            )
                            if (!isDeviceOnline) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE63946).copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "No Internet",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF6B6B)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isEffectiveOffline) "Playing only downloaded songs locally" else "Full library & high-res streaming",
                            fontSize = 11.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.65f)
                        )
                    }
                }

                Switch(
                    checked = isOfflineModeForced,
                    onCheckedChange = { onToggleOfflineMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0A0E17),
                        checkedTrackColor = theme.primaryColor,
                        uncheckedThumbColor = theme.onSurfaceColor.copy(alpha = 0.5f),
                        uncheckedTrackColor = theme.surfaceVariantColor
                    ),
                    modifier = Modifier.testTag("offline_mode_switch")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Storage Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.surfaceColor.copy(alpha = 0.6f))
                    .clickable { onOpenStorageManager() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SdStorage,
                        contentDescription = "Storage",
                        tint = theme.secondaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Offline Vault: ${storageStats.downloadedCount} tracks • %.1f MB".format(storageStats.totalSizeMb),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.onSurfaceColor.copy(alpha = 0.85f)
                    )
                }

                Text(
                    text = "Manage ➔",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primaryColor
                )
            }
        }
    }
}

/**
 * Dialog when user tries to play an undownloaded song in Offline Mode
 */
@Composable
fun OfflineNoticeDialog(
    message: String,
    theme: SHJTheme,
    onDismiss: () -> Unit,
    onDisableOfflineMode: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surfaceColor,
        icon = {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = theme.primaryColor,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Offline Mode Restriction",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.onSurfaceColor
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = theme.onSurfaceColor.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDisableOfflineMode()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
            ) {
                Text("Turn Off Offline Mode", color = Color(0xFF0A0E17), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = theme.onSurfaceColor.copy(alpha = 0.7f))
            }
        }
    )
}

/**
 * Storage Manager dialog for inspecting and clearing downloaded files
 */
@Composable
fun StorageManagerDialog(
    storageStats: StorageStats,
    theme: SHJTheme,
    onDismiss: () -> Unit,
    onClearAllDownloads: () -> Unit
) {
    var showConfirmClear by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = theme.surfaceColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = "Storage",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Offline Music Storage",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.onSurfaceColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.surfaceVariantColor.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("DOWNLOADED", fontSize = 10.sp, color = theme.primaryColor, fontWeight = FontWeight.Bold)
                            Text("${storageStats.downloadedCount} tracks", fontSize = 16.sp, fontWeight = FontWeight.Black, color = theme.onSurfaceColor)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.surfaceVariantColor.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("STORAGE USED", fontSize = 10.sp, color = theme.secondaryColor, fontWeight = FontWeight.Bold)
                            Text("%.1f MB".format(storageStats.totalSizeMb), fontSize = 16.sp, fontWeight = FontWeight.Black, color = theme.onSurfaceColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "High-definition lossless audio is cached safely on your device for offline playback without using mobile data.",
                    fontSize = 12.sp,
                    color = theme.onSurfaceColor.copy(alpha = 0.65f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (showConfirmClear) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE63946).copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Are you sure you want to delete all offline music downloads? You can re-download anytime.",
                            fontSize = 12.sp,
                            color = Color(0xFFFF6B6B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showConfirmClear = false }) {
                                Text("Cancel", color = theme.onSurfaceColor)
                            }
                            Button(
                                onClick = {
                                    showConfirmClear = false
                                    onClearAllDownloads()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                            ) {
                                Text("Delete All", color = Color.White)
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showConfirmClear = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5376)),
                        enabled = storageStats.downloadedCount > 0
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All Offline Downloads")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                ) {
                    Text("Close", color = Color(0xFF0A0E17), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Playlist detail view with full track listing and playlist download controls
 */
@Composable
fun PlaylistDetailDialog(
    playlist: Playlist,
    theme: SHJTheme,
    downloadingTrackIds: Map<String, Float>,
    isEffectiveOffline: Boolean,
    onDismiss: () -> Unit,
    onPlayTrack: (Track) -> Unit,
    onPlayAll: () -> Unit,
    onDownloadPlaylist: () -> Unit,
    onDeletePlaylistDownloads: () -> Unit,
    onDownloadTrack: (Track) -> Unit,
    onDeleteTrack: (Track) -> Unit
) {
    val context = LocalContext.current
    val imageResId = remember(playlist.coverDrawableName) {
        val name = playlist.coverDrawableName ?: "art_lofi_study"
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (id != 0) id else context.resources.getIdentifier("art_lofi_study", "drawable", context.packageName)
    }

    val downloadedCount = playlist.downloadedTracksCount
    val totalCount = playlist.tracks.size
    val isFullyDownloaded = playlist.isFullyDownloaded

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = theme.surfaceColor,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with artwork and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black)
                    ) {
                        if (imageResId != 0) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = playlist.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${playlist.tracks.size} tracks • ${playlist.formattedTotalDuration}",
                            fontSize = 12.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.65f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFullyDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                                contentDescription = null,
                                tint = if (isFullyDownloaded) Color(0xFF2EC4B6) else theme.primaryColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$downloadedCount/$totalCount Downloaded Offline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFullyDownloaded) Color(0xFF2EC4B6) else theme.primaryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row: Play All & Download All
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPlayAll,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF0A0E17), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play All", color = Color(0xFF0A0E17), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (isFullyDownloaded) {
                        OutlinedButton(
                            onClick = onDeletePlaylistDownloads,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5376))
                        ) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = onDownloadPlaylist,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariantColor)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download All", color = theme.primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tracklist
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(playlist.tracks) { track ->
                        val isTrackDownloading = downloadingTrackIds.containsKey(track.id)
                        val progress = downloadingTrackIds[track.id]
                        val isPlayableInOffline = !isEffectiveOffline || track.isDownloaded

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.surfaceVariantColor.copy(alpha = 0.3f))
                                .clickable { onPlayTrack(track) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (isPlayableInOffline) theme.primaryColor else theme.onSurfaceColor.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isPlayableInOffline) theme.onSurfaceColor else theme.onSurfaceColor.copy(alpha = 0.45f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${track.artist} • ${track.formattedDuration}",
                                    fontSize = 11.sp,
                                    color = theme.onSurfaceColor.copy(alpha = 0.5f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            DownloadStatusButton(
                                isDownloaded = track.isDownloaded,
                                downloadProgress = progress,
                                theme = theme,
                                onDownloadClick = { onDownloadTrack(track) },
                                onRemoveClick = { onDeleteTrack(track) }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Close", color = theme.onSurfaceColor.copy(alpha = 0.7f))
                }
            }
        }
    }
}
