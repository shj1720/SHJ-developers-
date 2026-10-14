package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SyncStatus
import com.example.viewmodel.MusicPlayerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudSyncScreen(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.currentTheme.collectAsState()
    val syncState by viewModel.cloudSyncState.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val context = LocalContext.current

    var remoteCodeInput by remember { mutableStateOf("") }
    var copiedNotice by remember { mutableStateOf(false) }

    val formattedTime = remember(syncState.lastSyncTime) {
        syncState.lastSyncTime?.let {
            SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(it))
        } ?: "Never"
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("cloud_sync_screen"),
        color = theme.backgroundColor
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Screen Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CLOUD SYNC HUB",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.onSurfaceColor
                        )
                        Text(
                            text = "Cross-Device Playlist Sync & Multi-Device Pairing",
                            fontSize = 12.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.65f)
                        )
                    }

                    Icon(
                        imageVector = if (syncState.status == SyncStatus.SYNCED) Icons.Default.CloudDone else Icons.Default.Sync,
                        contentDescription = "Sync Status",
                        tint = if (syncState.status == SyncStatus.SYNCED) theme.primaryColor else theme.secondaryColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Device Sync Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "THIS DEVICE SYNC CODE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryColor,
                                letterSpacing = 1.2.sp
                            )

                            Text(
                                text = syncState.deviceName,
                                fontSize = 11.sp,
                                color = theme.onSurfaceColor.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Code Box with Copy Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.surfaceVariantColor)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = syncState.syncCode,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = theme.primaryColor,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )

                            Row {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("SHJ Sync Code", syncState.syncCode)
                                        clipboard.setPrimaryClip(clip)
                                        copiedNotice = true
                                    },
                                    modifier = Modifier.testTag("copy_sync_code_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Code",
                                        tint = theme.primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.generateNewCloudSyncCode() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Autorenew,
                                        contentDescription = "New Code",
                                        tint = theme.onSurfaceColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        if (copiedNotice) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "✓ Sync code copied to clipboard! Paste on another device to link.",
                                fontSize = 11.sp,
                                color = theme.primaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Auto-Sync Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Auto-Sync Changes",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onSurfaceColor
                                )
                                Text(
                                    text = "Push playlists automatically when modified",
                                    fontSize = 11.sp,
                                    color = theme.onSurfaceColor.copy(alpha = 0.6f)
                                )
                            }

                            Switch(
                                checked = syncState.isAutoSyncEnabled,
                                onCheckedChange = { viewModel.cloudSyncManager.toggleAutoSync(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = theme.primaryColor,
                                    checkedTrackColor = theme.primaryColor.copy(alpha = 0.4f),
                                    uncheckedThumbColor = theme.onSurfaceColor.copy(alpha = 0.5f),
                                    uncheckedTrackColor = theme.surfaceVariantColor
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (syncState.status == SyncStatus.SYNCING) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = theme.primaryColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "${syncState.statusMessage} • Last synced: $formattedTime",
                                fontSize = 11.sp,
                                color = theme.onSurfaceColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Push & Pull
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Push to Cloud
                    Button(
                        onClick = { viewModel.pushPlaylistsToCloud() },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("push_to_cloud_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Push",
                            tint = Color(0xFF0A0E17),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Push to Cloud",
                            color = Color(0xFF0A0E17),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pull from another device
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "PULL FROM ANOTHER DEVICE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.secondaryColor,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = remoteCodeInput,
                                onValueChange = { remoteCodeInput = it.uppercase() },
                                placeholder = { Text("e.g. SHJ-9104-AZ", fontSize = 13.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = theme.secondaryColor,
                                    unfocusedBorderColor = theme.surfaceVariantColor
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("remote_sync_code_input")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (remoteCodeInput.isNotBlank()) {
                                        viewModel.pullPlaylistsFromCloud(remoteCodeInput.trim())
                                        remoteCodeInput = ""
                                    }
                                },
                                enabled = remoteCodeInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.secondaryColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("pull_from_cloud_button")
                            ) {
                                Text("Pull Playlists", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Cloud Snapshot History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CLOUD SNAPSHOT ARCHIVES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryColor,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // List of snapshots
            items(syncState.cloudSnapshots) { snap ->
                val snapDate = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(snap.timestamp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = snap.deviceName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor
                            )
                            Text(
                                text = snap.summary,
                                fontSize = 11.sp,
                                color = theme.onSurfaceColor.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$snapDate • ${snap.playlistCount} Playlists (${snap.totalTracks} Tracks)",
                                fontSize = 10.sp,
                                color = theme.primaryColor
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.pullPlaylistsFromCloud(snap.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariantColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Restore", fontSize = 11.sp, color = theme.onSurfaceColor)
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
