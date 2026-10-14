package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import coil.compose.AsyncImage
import com.example.R
import com.example.model.SyncStatus
import com.example.viewmodel.MusicPlayerViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "CloudSyncScreen"

@Composable
fun CloudSyncScreen(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.currentTheme.collectAsState()
    val syncState by viewModel.cloudSyncState.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val firebaseUser by viewModel.firebaseUser.collectAsState()
    val firebaseSyncMessage by viewModel.firebaseSyncMessage.collectAsState()
    val isFirebaseSyncing by viewModel.isFirebaseSyncing.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var remoteCodeInput by remember { mutableStateOf("") }
    var copiedNotice by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

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
                            text = "Firebase Firestore & Multi-Device Sync",
                            fontSize = 12.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.65f)
                        )
                    }

                    Icon(
                        imageVector = if (firebaseUser != null) Icons.Default.CloudDone else Icons.Default.Sync,
                        contentDescription = "Sync Status",
                        tint = if (firebaseUser != null) theme.primaryColor else theme.secondaryColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Firebase Syncing progress indicator
                if (isFirebaseSyncing) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = theme.primaryColor,
                        trackColor = theme.surfaceVariantColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Status message banner
                firebaseSyncMessage?.let { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.primaryColor.copy(alpha = 0.15f))
                            .border(1.dp, theme.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = theme.onSurfaceColor,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.dismissFirebaseSyncMessage() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = theme.onSurfaceColor, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                authError?.let { err ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE63946).copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = err,
                            fontSize = 12.sp,
                            color = Color(0xFFFF6B6B),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { authError = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFFFF6B6B), modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // ----------------------------------------------------
                // Section 1: Firebase Authentication & Firestore Cloud
                // ----------------------------------------------------
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_account_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (firebaseUser != null) Color(0xFF2EC4B6).copy(alpha = 0.5f) else theme.primaryColor.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FIREBASE CLOUD ACCOUNT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (firebaseUser != null) Color(0xFF2EC4B6) else theme.primaryColor,
                                letterSpacing = 1.2.sp
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (firebaseUser != null) Color(0xFF2EC4B6).copy(alpha = 0.15f)
                                        else theme.surfaceVariantColor
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (firebaseUser != null) "Connected" else "Not Signed In",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (firebaseUser != null) Color(0xFF2EC4B6) else theme.onSurfaceColor.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (firebaseUser != null) {
                            // User is Authenticated: Profile Info & Backup/Restore controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!firebaseUser?.photoUrl?.toString().isNullOrEmpty()) {
                                    AsyncImage(
                                        model = firebaseUser?.photoUrl,
                                        contentDescription = "User profile avatar",
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, Color(0xFF2EC4B6), CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2EC4B6).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "User",
                                            tint = Color(0xFF2EC4B6),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = firebaseUser?.displayName ?: "Google User",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.onSurfaceColor
                                    )
                                    Text(
                                        text = firebaseUser?.email ?: "",
                                        fontSize = 12.sp,
                                        color = theme.onSurfaceColor.copy(alpha = 0.65f)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.signOutFirebase() },
                                    modifier = Modifier.testTag("sign_out_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Sign Out",
                                        tint = theme.onSurfaceColor.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Your playlists and custom sound profiles are securely linked to your Firebase Firestore database.",
                                fontSize = 12.sp,
                                color = theme.onSurfaceColor.copy(alpha = 0.75f)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons: Backup and Restore
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.pushPlaylistsToFirebase() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("backup_to_firestore_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                    enabled = !isFirebaseSyncing
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF0A0E17), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Backup to Cloud", color = Color(0xFF0A0E17), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.pullPlaylistsFromFirebase() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("restore_from_firestore_button"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.primaryColor),
                                    enabled = !isFirebaseSyncing
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restore Library", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // User is NOT Authenticated: Prompt Google Sign-In
                            Text(
                                text = "Connect your Google Account to automatically sync playlists, custom sound presets, and listening history with Firebase Firestore across all your phones and tablets.",
                                fontSize = 12.sp,
                                color = theme.onSurfaceColor.copy(alpha = 0.75f),
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            authError = null
                                            val credentialManager = CredentialManager.create(context)
                                            val webClientId = context.getString(R.string.default_web_client_id)

                                            val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
                                                .build()

                                            val request = GetCredentialRequest.Builder()
                                                .addCredentialOption(googleIdOption)
                                                .build()

                                            val result = credentialManager.getCredential(
                                                request = request,
                                                context = context
                                            )
                                            val credential = result.credential
                                            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                                val idToken = googleIdTokenCredential.idToken
                                                viewModel.signInWithGoogle(idToken) { success, err ->
                                                    if (!success && err != null) {
                                                        authError = err
                                                    }
                                                }
                                            } else {
                                                authError = "Unexpected credential returned: ${credential.type}"
                                            }
                                        } catch (e: GetCredentialCancellationException) {
                                            Log.w(TAG, "Sign-in cancelled by user")
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Google Sign-in failed", e)
                                            authError = "Sign-in failed: ${e.localizedMessage ?: "Unknown error"}"
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("google_sign_in_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Google",
                                        tint = Color(0xFF0A0E17),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sign in with Google",
                                        color = Color(0xFF0A0E17),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ----------------------------------------------------
                // Section 2: Device Pairing Code & Offline Sharing
                // ----------------------------------------------------
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
                                text = "DEVICE PAIRING CODE",
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
                                text = "✓ Sync code copied to clipboard! Share with another device to link.",
                                fontSize = 11.sp,
                                color = theme.primaryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pair Remote Device Input Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "LINK REMOTE DEVICE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.secondaryColor,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enter the sync code shown on your laptop, tablet, or another phone to pull its playlists.",
                            fontSize = 12.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.65f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = remoteCodeInput,
                                onValueChange = { remoteCodeInput = it.uppercase() },
                                placeholder = { Text("e.g. SHJ-4829-AZ", fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
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
                                        viewModel.pullPlaylistsFromCloud(remoteCodeInput)
                                        remoteCodeInput = ""
                                    }
                                },
                                enabled = remoteCodeInput.length >= 6,
                                colors = ButtonDefaults.buttonColors(containerColor = theme.secondaryColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("pull_remote_button")
                            ) {
                                Text("Link & Pull", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Cloud Sync History & Snapshots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DEVICE ACTIVITY & SNAPSHOTS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryColor,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Last: $formattedTime",
                        fontSize = 11.sp,
                        color = theme.onSurfaceColor.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // List of Recent Sync Snapshots
            items(syncState.cloudSnapshots) { snapshot ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor.copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.primaryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = "Device",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = snapshot.deviceName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor
                            )
                            Text(
                                text = snapshot.summary,
                                fontSize = 11.sp,
                                color = theme.onSurfaceColor.copy(alpha = 0.65f),
                                maxLines = 1
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.surfaceVariantColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${snapshot.playlistCount} sets",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.primaryColor
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}
