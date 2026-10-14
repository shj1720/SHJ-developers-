package com.example.model

enum class SyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    FAILED
}

data class CloudSyncSnapshot(
    val id: String,
    val deviceName: String,
    val timestamp: Long,
    val playlistCount: Int,
    val totalTracks: Int,
    val summary: String
)

data class CloudSyncState(
    val syncCode: String = "SHJ-7492-X1",
    val deviceName: String = "SHJ Android Master",
    val isAutoSyncEnabled: Boolean = true,
    val lastSyncTime: Long? = System.currentTimeMillis() - 3600000L,
    val status: SyncStatus = SyncStatus.SYNCED,
    val statusMessage: String = "All playlists up to date on cloud",
    val cloudSnapshots: List<CloudSyncSnapshot> = emptyList()
)
