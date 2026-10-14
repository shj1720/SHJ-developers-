package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExportFormat
import com.example.data.SampleMusicRepository
import com.example.model.Playlist
import com.example.viewmodel.MusicPlayerViewModel

@Composable
fun SpotifyHubScreen(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.currentTheme.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val downloadProgress by viewModel.spotifyManager.downloadProgress.collectAsState()

    var inputUrl by remember { mutableStateOf("") }
    var selectedPlaylistForExport by remember { mutableStateOf<Playlist?>(null) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("spotify_hub_screen"),
        color = theme.backgroundColor
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SPOTIFY HUB",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.onSurfaceColor
                        )
                        Text(
                            text = "Import, Batch Download & Universal Playlist Export",
                            fontSize = 12.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.65f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1DB954).copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Spotify Ready",
                            color = Color(0xFF1DB954),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Import Link Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "IMPORT SPOTIFY PLAYLIST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryColor,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            placeholder = { Text("Paste https://open.spotify.com/playlist/...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Link, contentDescription = "Link", tint = theme.primaryColor)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.primaryColor,
                                unfocusedBorderColor = theme.surfaceVariantColor
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("spotify_link_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quick sample pills
                            Text(
                                text = "Paste sample link",
                                fontSize = 11.sp,
                                color = theme.secondaryColor,
                                modifier = Modifier
                                    .clickable {
                                        inputUrl = "https://open.spotify.com/playlist/37i9dQZF1DXdLEN7aqioXM"
                                    }
                                    .padding(4.dp)
                            )

                            Button(
                                onClick = {
                                    if (inputUrl.isNotBlank()) {
                                        viewModel.importSpotifyPlaylist(inputUrl)
                                        inputUrl = ""
                                    }
                                },
                                enabled = inputUrl.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("import_spotify_button")
                            ) {
                                Text("Import Playlist", color = Color(0xFF0A0E17), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Live Batch Downloader Card (Download All at Once)
                AnimatedVisibility(visible = downloadProgress.isDownloading || downloadProgress.isFinished) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (downloadProgress.isFinished) Color(0xFF10281E) else theme.surfaceColor
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (downloadProgress.isFinished) Color(0xFF2EC4B6) else theme.primaryColor
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (downloadProgress.isFinished) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Finished",
                                            tint = Color(0xFF2EC4B6),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.5.dp,
                                            color = theme.primaryColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (downloadProgress.isFinished) "BATCH DOWNLOAD COMPLETE" else "DOWNLOADING ALL TRACKS",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (downloadProgress.isFinished) Color(0xFF2EC4B6) else theme.primaryColor
                                    )
                                }

                                Text(
                                    text = "${(downloadProgress.overallPercentage * 100).toInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = theme.onSurfaceColor
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { downloadProgress.overallPercentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (downloadProgress.isFinished) Color(0xFF2EC4B6) else theme.primaryColor,
                                trackColor = theme.surfaceVariantColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = downloadProgress.currentTrackTitle,
                                    fontSize = 12.sp,
                                    color = theme.onSurfaceColor.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${downloadProgress.completedTracks}/${downloadProgress.totalTracks} cached",
                                    fontSize = 11.sp,
                                    color = theme.onSurfaceColor.copy(alpha = 0.6f)
                                )
                            }

                            if (downloadProgress.isFinished) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "All tracks are now saved offline with high-fidelity audio cache!",
                                    fontSize = 11.sp,
                                    color = Color(0xFF80ED99)
                                )
                            }
                        }
                    }
                }

                // Export Playlist Notice
                exportSuccessMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.primaryColor.copy(alpha = 0.15f))
                            .border(1.dp, theme.primaryColor, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = theme.primaryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PLAYLISTS & BATCH ACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primaryColor,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // List of Playlists with "Download All at Once" & "Export" buttons
            items(playlists) { playlist ->
                SpotifyPlaylistCard(
                    playlist = playlist,
                    theme = theme,
                    onPlay = { viewModel.playPlaylist(playlist) },
                    onDownloadAll = {
                        viewModel.downloadAllPlaylistTracks(playlist)
                    },
                    onExportJson = {
                        val exported = viewModel.exportPlaylist(playlist, ExportFormat.JSON)
                        exportSuccessMessage = "Exported '${playlist.name}' as JSON and shared!"
                    },
                    onExportM3u = {
                        val exported = viewModel.exportPlaylist(playlist, ExportFormat.M3U)
                        exportSuccessMessage = "Exported '${playlist.name}' as M3U8 playlist!"
                    },
                    onExportText = {
                        val exported = viewModel.exportPlaylist(playlist, ExportFormat.TEXT_LIST)
                        exportSuccessMessage = "Exported '${playlist.name}' tracklist!"
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Curated Spotify Sets to add
                Text(
                    text = "RECOMMENDED SPOTIFY CURATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.secondaryColor,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            items(SampleMusicRepository.spotifyCuratedPresets) { curated ->
                val alreadyAdded = playlists.any { it.name == curated.name }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = curated.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor
                            )
                            Text(
                                text = curated.description,
                                fontSize = 11.sp,
                                color = theme.onSurfaceColor.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = {
                                if (!alreadyAdded) {
                                    viewModel.importSpotifyPlaylist(curated.spotifyUrl ?: "")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (alreadyAdded) theme.surfaceVariantColor else theme.secondaryColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = if (alreadyAdded) "Added" else "Add Set",
                                fontSize = 11.sp,
                                color = if (alreadyAdded) theme.onSurfaceColor.copy(alpha = 0.6f) else Color.White
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

@Composable
fun SpotifyPlaylistCard(
    playlist: Playlist,
    theme: com.example.model.SHJTheme,
    onPlay: () -> Unit,
    onDownloadAll: () -> Unit,
    onExportJson: () -> Unit,
    onExportM3u: () -> Unit,
    onExportText: () -> Unit
) {
    val context = LocalContext.current
    var showExportMenu by remember { mutableStateOf(false) }

    val imageResId = remember(playlist.coverDrawableName) {
        val name = playlist.coverDrawableName ?: "art_lofi_study"
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (id != 0) id else context.resources.getIdentifier("art_lofi_study", "drawable", context.packageName)
    }

    val downloadedCount = playlist.tracks.count { it.isDownloaded }
    val allDownloaded = downloadedCount == playlist.tracks.size && playlist.tracks.isNotEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("playlist_card_${playlist.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cover
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                        fontSize = 15.sp,
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
                    if (allDownloaded) {
                        Text(
                            text = "✓ All tracks downloaded offline",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2EC4B6)
                        )
                    }
                }

                // Play Button
                IconButton(
                    onClick = onPlay,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(theme.primaryColor)
                        .testTag("play_playlist_${playlist.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color(0xFF0A0E17),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row: "Download All at Once" & "Export"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Download All at Once Button
                Button(
                    onClick = onDownloadAll,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allDownloaded) theme.surfaceVariantColor else theme.primaryColor.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("download_all_button_${playlist.id}")
                ) {
                    Icon(
                        imageVector = if (allDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                        contentDescription = "Download All",
                        tint = if (allDownloaded) Color(0xFF2EC4B6) else theme.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (allDownloaded) "Downloaded" else "Download All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (allDownloaded) Color(0xFF2EC4B6) else theme.primaryColor
                    )
                }

                // Export Dropdown / Action
                Button(
                    onClick = { showExportMenu = !showExportMenu },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariantColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("export_menu_button_${playlist.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export",
                        tint = theme.onSurfaceColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Export",
                        fontSize = 12.sp,
                        color = theme.onSurfaceColor
                    )
                }
            }

            // Export format selector expansion
            AnimatedVisibility(visible = showExportMenu) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.surfaceVariantColor.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Choose Export Format:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                onExportM3u()
                                showExportMenu = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("M3U8", fontSize = 11.sp, color = theme.onSurfaceColor)
                        }
                        Button(
                            onClick = {
                                onExportJson()
                                showExportMenu = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("JSON", fontSize = 11.sp, color = theme.onSurfaceColor)
                        }
                        Button(
                            onClick = {
                                onExportText()
                                showExportMenu = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Text", fontSize = 11.sp, color = theme.onSurfaceColor)
                        }
                    }
                }
            }
        }
    }
}
