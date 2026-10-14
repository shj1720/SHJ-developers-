package com.example.data

import com.example.model.CloudSyncSnapshot
import com.example.model.CloudSyncState
import com.example.model.Playlist
import com.example.model.SyncStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CloudSyncManager {

    private val _syncState = MutableStateFlow(
        CloudSyncState(
            syncCode = "SHJ-8824-CY",
            deviceName = "Android Mobile (SHJ Client)",
            isAutoSyncEnabled = true,
            lastSyncTime = System.currentTimeMillis() - 1800000L,
            status = SyncStatus.SYNCED,
            statusMessage = "Synced with Cloud Hub (All devices up to date)",
            cloudSnapshots = listOf(
                CloudSyncSnapshot(
                    id = "snap_01",
                    deviceName = "SHJ Desktop Hub",
                    timestamp = System.currentTimeMillis() - 86400000L,
                    playlistCount = 4,
                    totalTracks = 24,
                    summary = "Initial sync of Spotify Lo-Fi & Synthwave sets"
                ),
                CloudSyncSnapshot(
                    id = "snap_02",
                    deviceName = "Android Tablet",
                    timestamp = System.currentTimeMillis() - 14400000L,
                    playlistCount = 5,
                    totalTracks = 32,
                    summary = "Added Dusty Vinyl Sessions + EQ Lo-Fi Profile"
                )
            )
        )
    )
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    fun generateNewSyncCode(): String {
        val randomChars = (1..4).map { ('A'..'Z').random() }.joinToString("")
        val randomDigits = (1000..9999).random()
        val code = "SHJ-$randomDigits-$randomChars"
        _syncState.value = _syncState.value.copy(syncCode = code)
        return code
    }

    suspend fun pushToCloud(playlists: List<Playlist>) {
        _syncState.value = _syncState.value.copy(
            status = SyncStatus.SYNCING,
            statusMessage = "Uploading ${playlists.size} playlists to Cloud Hub..."
        )
        delay(1200)

        val totalTracks = playlists.sumOf { it.tracks.size }
        val newSnapshot = CloudSyncSnapshot(
            id = "snap_${UUID.randomUUID().toString().take(6)}",
            deviceName = _syncState.value.deviceName,
            timestamp = System.currentTimeMillis(),
            playlistCount = playlists.size,
            totalTracks = totalTracks,
            summary = "Push update: ${playlists.size} playlists ($totalTracks tracks) synchronized"
        )

        val updatedSnapshots = listOf(newSnapshot) + _syncState.value.cloudSnapshots

        _syncState.value = _syncState.value.copy(
            status = SyncStatus.SYNCED,
            lastSyncTime = System.currentTimeMillis(),
            statusMessage = "Cloud sync successful! Synchronized across devices.",
            cloudSnapshots = updatedSnapshots
        )
    }

    suspend fun pullFromCloud(
        syncCodeToPull: String,
        currentPlaylists: List<Playlist>
    ): List<Playlist> {
        _syncState.value = _syncState.value.copy(
            status = SyncStatus.SYNCING,
            statusMessage = "Resolving cloud snapshot for $syncCodeToPull..."
        )
        delay(1400)

        // Generate incoming playlists from remote device
        val samplePulledPlaylist = SampleMusicRepository.spotifyCuratedPresets[0].copy(
            id = "pulled_remote_${UUID.randomUUID().toString().take(6)}",
            name = "Cloud Synced: $syncCodeToPull",
            description = "Pulled from remote paired device ($syncCodeToPull)",
            isSyncedToCloud = true
        )

        val merged = currentPlaylists.toMutableList().apply {
            if (none { it.name == samplePulledPlaylist.name }) {
                add(samplePulledPlaylist)
            }
        }

        _syncState.value = _syncState.value.copy(
            status = SyncStatus.SYNCED,
            lastSyncTime = System.currentTimeMillis(),
            statusMessage = "Playlists pulled successfully from $syncCodeToPull!"
        )

        return merged
    }

    fun toggleAutoSync(enabled: Boolean) {
        _syncState.value = _syncState.value.copy(isAutoSyncEnabled = enabled)
    }
}
