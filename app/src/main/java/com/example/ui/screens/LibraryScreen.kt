package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.model.Playlist
import com.example.model.SHJTheme
import com.example.model.Track
import com.example.ui.components.DownloadStatusButton
import com.example.ui.components.OfflineModeBanner
import com.example.ui.components.OfflineNoticeDialog
import com.example.ui.components.PlaylistDetailDialog
import com.example.ui.components.StorageManagerDialog
import com.example.viewmodel.MusicPlayerViewModel

@Composable
fun LibraryScreen(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.currentTheme.collectAsState()
    val allTracks by viewModel.allTracks.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    // Offline & Download states
    val isOfflineModeForced by viewModel.isOfflineModeForced.collectAsState()
    val isDeviceOnline by viewModel.isDeviceOnline.collectAsState()
    val isEffectiveOffline by viewModel.isEffectiveOffline.collectAsState()
    val downloadingTrackIds by viewModel.downloadingTrackIds.collectAsState()
    val storageStats by viewModel.storageStats.collectAsState()
    val offlineNoticeMessage by viewModel.offlineNoticeMessage.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Multi-Select state
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedTrackIds by remember { mutableStateOf(setOf<String>()) }

    // Dialogs state
    var showStorageDialog by remember { mutableStateOf(false) }
    var viewingPlaylistDetail by remember { mutableStateOf<Playlist?>(null) }

    val tabs = listOf("All Tracks", "Playlists", "Favorites", "Downloaded")

    val filteredTracks = remember(allTracks, selectedTabIndex, searchQuery) {
        val baseList = when (selectedTabIndex) {
            0 -> allTracks
            2 -> allTracks.filter { it.isFavorite }
            3 -> allTracks.filter { it.isDownloaded }
            else -> allTracks
        }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.genre.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen"),
        color = theme.backgroundColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Header Row with App Title and Multi-Select action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MUSIC LIBRARY",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.onSurfaceColor
                        )
                        Text(
                            text = "${allTracks.size} Master Tracks • ${storageStats.downloadedCount} Offline Ready",
                            fontSize = 12.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.65f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Multi-selection Toggle Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelectionMode) theme.primaryColor else theme.surfaceColor)
                                .clickable {
                                    isSelectionMode = !isSelectionMode
                                    if (!isSelectionMode) selectedTrackIds = emptySet()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("toggle_select_mode_button")
                        ) {
                            Text(
                                text = if (isSelectionMode) "Done" else "Select",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelectionMode) Color(0xFF0A0E17) else theme.primaryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Offline Mode Banner with Switch & Storage Metrics
                OfflineModeBanner(
                    isOfflineModeForced = isOfflineModeForced,
                    isDeviceOnline = isDeviceOnline,
                    isEffectiveOffline = isEffectiveOffline,
                    storageStats = storageStats,
                    theme = theme,
                    onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                    onOpenStorageManager = { showStorageDialog = true }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search songs, artists, or genres...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = theme.primaryColor
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = theme.onSurfaceColor.copy(alpha = 0.5f)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.surfaceVariantColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Tabs Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = theme.primaryColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = theme.primaryColor
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, tabTitle ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tabTitle,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTabIndex == index) theme.primaryColor else theme.onSurfaceColor.copy(alpha = 0.6f)
                                    )
                                    if (index == 3 && storageStats.downloadedCount > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(Color(0xFF2EC4B6))
                                                .size(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${storageStats.downloadedCount}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0A0E17)
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Content List
                if (selectedTabIndex == 1) {
                    // Playlists List
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(playlists) { playlist ->
                            PlaylistRowItem(
                                playlist = playlist,
                                theme = theme,
                                isEffectiveOffline = isEffectiveOffline,
                                onPlay = { viewModel.playPlaylist(playlist) },
                                onOpenDetails = { viewingPlaylistDetail = playlist },
                                onDownloadPlaylist = { viewModel.downloadPlaylist(playlist) },
                                onDeleteDownloads = { viewModel.deleteDownloadedPlaylist(playlist) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                } else {
                    // Tracks List (All, Favorites, or Downloaded)
                    if (selectedTabIndex == 3 && filteredTracks.isEmpty()) {
                        // Empty Downloaded State
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Offline Tracks Yet",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onSurfaceColor
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap the download icon on any song or playlist to save it for listening when offline without internet.",
                                    fontSize = 12.sp,
                                    color = theme.onSurfaceColor.copy(alpha = 0.65f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        // Download first 3 tracks
                                        viewModel.downloadSelectedTracks(allTracks.take(3))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF0A0E17), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download Starter Pack (3 Songs)", color = Color(0xFF0A0E17), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Action row if on "Downloaded" tab
                        if (selectedTabIndex == 3 && filteredTracks.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${filteredTracks.size} tracks • Offline Ready",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2EC4B6)
                                )
                                Button(
                                    onClick = {
                                        viewModel.playTrack(filteredTracks.first(), filteredTracks)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF0A0E17), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play All Offline", color = Color(0xFF0A0E17), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredTracks) { track ->
                                val isCurrent = track.id == currentTrack.id
                                val isDownloading = downloadingTrackIds.containsKey(track.id)
                                val downloadProgress = downloadingTrackIds[track.id]
                                val isSelected = selectedTrackIds.contains(track.id)

                                TrackRowItem(
                                    track = track,
                                    isCurrent = isCurrent,
                                    isPlaying = isPlaying && isCurrent,
                                    isEffectiveOffline = isEffectiveOffline,
                                    downloadProgress = downloadProgress,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = isSelected,
                                    theme = theme,
                                    onPlay = { viewModel.playTrack(track, filteredTracks) },
                                    onToggleFavorite = { viewModel.toggleFavorite(track) },
                                    onDownload = { viewModel.downloadTrack(track) },
                                    onDeleteDownload = { viewModel.deleteDownloadedTrack(track) },
                                    onToggleSelect = {
                                        selectedTrackIds = if (isSelected) {
                                            selectedTrackIds - track.id
                                        } else {
                                            selectedTrackIds + track.id
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            item {
                                Spacer(modifier = Modifier.height(90.dp))
                            }
                        }
                    }
                }
            }

            // Floating Multi-Select Action Bar at bottom
            if (isSelectionMode && selectedTrackIds.isNotEmpty()) {
                val selectedTracks = allTracks.filter { selectedTrackIds.contains(it.id) }
                val undownloadedInSelection = selectedTracks.filter { !it.isDownloaded }
                val downloadedInSelection = selectedTracks.filter { it.isDownloaded }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 70.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.surfaceColor)
                        .border(1.dp, theme.primaryColor, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedTrackIds.size} Selected",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor
                            )
                            Text(
                                text = "${downloadedInSelection.size} offline, ${undownloadedInSelection.size} online",
                                fontSize = 10.sp,
                                color = theme.onSurfaceColor.copy(alpha = 0.6f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (undownloadedInSelection.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        viewModel.downloadSelectedTracks(undownloadedInSelection)
                                        isSelectionMode = false
                                        selectedTrackIds = emptySet()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF0A0E17), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download (${undownloadedInSelection.size})", color = Color(0xFF0A0E17), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (downloadedInSelection.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.deleteSelectedTracks(downloadedInSelection)
                                        isSelectionMode = false
                                        selectedTrackIds = emptySet()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5376))
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete (${downloadedInSelection.size})", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Storage Management Dialog
            if (showStorageDialog) {
                StorageManagerDialog(
                    storageStats = storageStats,
                    theme = theme,
                    onDismiss = { showStorageDialog = false },
                    onClearAllDownloads = { viewModel.clearAllDownloads() }
                )
            }

            // Playlist Detail Dialog
            viewingPlaylistDetail?.let { pl ->
                PlaylistDetailDialog(
                    playlist = pl,
                    theme = theme,
                    downloadingTrackIds = downloadingTrackIds,
                    isEffectiveOffline = isEffectiveOffline,
                    onDismiss = { viewingPlaylistDetail = null },
                    onPlayTrack = { tr -> viewModel.playTrack(tr, pl.tracks) },
                    onPlayAll = { viewModel.playPlaylist(pl) },
                    onDownloadPlaylist = { viewModel.downloadPlaylist(pl) },
                    onDeletePlaylistDownloads = { viewModel.deleteDownloadedPlaylist(pl) },
                    onDownloadTrack = { tr -> viewModel.downloadTrack(tr) },
                    onDeleteTrack = { tr -> viewModel.deleteDownloadedTrack(tr) }
                )
            }

            // Offline Notice Dialog (when user attempts to play undownloaded track in offline mode)
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

@Composable
fun TrackRowItem(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isEffectiveOffline: Boolean,
    downloadProgress: Float?,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    theme: SHJTheme,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownload: () -> Unit,
    onDeleteDownload: () -> Unit,
    onToggleSelect: () -> Unit
) {
    val context = LocalContext.current
    val imageResId = remember(track.albumArtDrawableName) {
        val name = track.albumArtDrawableName ?: "art_lofi_study"
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (id != 0) id else context.resources.getIdentifier("art_lofi_study", "drawable", context.packageName)
    }

    // If offline mode is active and track is not downloaded, visually differentiate as restricted
    val isRestrictedOffline = isEffectiveOffline && !track.isDownloaded

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isSelectionMode) onToggleSelect() else onPlay()
            }
            .testTag("track_row_${track.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> theme.primaryColor.copy(alpha = 0.25f)
                isCurrent -> theme.primaryColor.copy(alpha = 0.15f)
                isRestrictedOffline -> theme.surfaceColor.copy(alpha = 0.5f)
                else -> theme.surfaceColor
            }
        ),
        border = when {
            isSelected -> androidx.compose.foundation.BorderStroke(1.5.dp, theme.primaryColor)
            isCurrent -> androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor)
            else -> null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox if in multi-selection mode
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = theme.primaryColor,
                        checkmarkColor = Color(0xFF0A0E17),
                        uncheckedColor = theme.onSurfaceColor.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Artwork
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Playing",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title, Artist, & Badges
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontSize = 14.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                    color = when {
                        isCurrent -> theme.primaryColor
                        isRestrictedOffline -> theme.onSurfaceColor.copy(alpha = 0.5f)
                        else -> theme.onSurfaceColor
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${track.artist} • ${track.formattedDuration}",
                        fontSize = 12.sp,
                        color = theme.onSurfaceColor.copy(alpha = if (isRestrictedOffline) 0.4f else 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (track.isDownloaded) {
                        // Visual "Offline Ready" Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2EC4B6).copy(alpha = 0.18f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Downloaded",
                                    tint = Color(0xFF2EC4B6),
                                    modifier = Modifier.size(11.dp)
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
                    } else if (isRestrictedOffline) {
                        // Online Only Indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF9F1C).copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Online Only",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9F1C)
                            )
                        }
                    }
                }
            }

            // Download Status Button
            DownloadStatusButton(
                isDownloaded = track.isDownloaded,
                downloadProgress = downloadProgress,
                theme = theme,
                onDownloadClick = onDownload,
                onRemoveClick = onDeleteDownload
            )

            // Favorite Button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (track.isFavorite) theme.secondaryColor else theme.onSurfaceColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PlaylistRowItem(
    playlist: Playlist,
    theme: SHJTheme,
    isEffectiveOffline: Boolean,
    onPlay: () -> Unit,
    onOpenDetails: () -> Unit,
    onDownloadPlaylist: () -> Unit,
    onDeleteDownloads: () -> Unit
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
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
                    fontSize = 14.sp,
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
                // Download Status Pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFullyDownloaded) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Fully downloaded",
                            tint = Color(0xFF2EC4B6),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "All Downloaded (Offline Ready)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2EC4B6)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Partially downloaded",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$downloadedCount/$totalCount Downloaded",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.onSurfaceColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Quick Download / Delete Playlist Button
            if (isFullyDownloaded) {
                IconButton(
                    onClick = onDeleteDownloads,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Downloaded playlist",
                        tint = Color(0xFF2EC4B6),
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onDownloadPlaylist,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download entire playlist",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Play Playlist Button
            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(theme.primaryColor)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color(0xFF0A0E17),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
