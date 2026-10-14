package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.model.Playlist
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class StorageStats(
    val downloadedCount: Int,
    val totalSizeMb: Float,
    val availableSpaceMb: Float
)

class OfflineDownloadManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val prefs = context.getSharedPreferences("shj_offline_storage", Context.MODE_PRIVATE)

    private val downloadDir = File(context.filesDir, "shj_offline_music").apply {
        if (!exists()) mkdirs()
    }

    // Persisted Set of Downloaded Track IDs
    private val _downloadedTrackIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedTrackIds: StateFlow<Set<String>> = _downloadedTrackIds.asStateFlow()

    // Active download progress per track ID (0.0f .. 1.0f)
    private val _downloadingTrackIds = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadingTrackIds: StateFlow<Map<String, Float>> = _downloadingTrackIds.asStateFlow()

    // Offline Mode toggles
    private val _isOfflineModeForced = MutableStateFlow(false)
    val isOfflineModeForced: StateFlow<Boolean> = _isOfflineModeForced.asStateFlow()

    private val _isDeviceOnline = MutableStateFlow(true)
    val isDeviceOnline: StateFlow<Boolean> = _isDeviceOnline.asStateFlow()

    private val _isEffectiveOffline = MutableStateFlow(false)
    val isEffectiveOffline: StateFlow<Boolean> = _isEffectiveOffline.asStateFlow()

    init {
        loadPersistedDownloads()
        setupNetworkMonitoring()
        updateEffectiveOffline()
    }

    private fun loadPersistedDownloads() {
        val saved = prefs.getStringSet("downloaded_ids", null)
        if (saved == null) {
            // First run: seed with default downloaded sample tracks
            val initial = SampleMusicRepository.builtInTracks
                .filter { it.isDownloaded }
                .map { it.id }
                .toSet()
            _downloadedTrackIds.value = initial
            persistDownloadedIds(initial)
            // Create initial placeholder cache files
            SampleMusicRepository.builtInTracks.filter { it.isDownloaded }.forEach { track ->
                createLocalCacheFile(track)
            }
        } else {
            _downloadedTrackIds.value = saved
        }
    }

    private fun persistDownloadedIds(ids: Set<String>) {
        prefs.edit().putStringSet("downloaded_ids", ids).apply()
    }

    private fun setupNetworkMonitoring() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm == null) {
            _isDeviceOnline.value = true
            return
        }

        try {
            val activeNet = cm.activeNetwork
            val caps = cm.getNetworkCapabilities(activeNet)
            val isConnected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            _isDeviceOnline.value = isConnected

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isDeviceOnline.value = true
                    updateEffectiveOffline()
                }

                override fun onLost(network: Network) {
                    _isDeviceOnline.value = false
                    updateEffectiveOffline()
                }
            })
        } catch (e: Exception) {
            Log.w("OfflineDownloadManager", "Network monitor setup failed: ${e.message}")
            _isDeviceOnline.value = true
        }
    }

    private fun updateEffectiveOffline() {
        _isEffectiveOffline.value = _isOfflineModeForced.value || !_isDeviceOnline.value
    }

    fun setOfflineModeForced(forced: Boolean) {
        _isOfflineModeForced.value = forced
        updateEffectiveOffline()
    }

    fun toggleOfflineMode(): Boolean {
        val newValue = !_isOfflineModeForced.value
        setOfflineModeForced(newValue)
        return newValue
    }

    fun isTrackDownloaded(trackId: String): Boolean {
        return _downloadedTrackIds.value.contains(trackId)
    }

    suspend fun downloadTrack(
        track: Track,
        onComplete: (Track) -> Unit
    ) {
        downloadTrack(track = track, onProgress = null, onComplete = onComplete)
    }

    /**
     * Downloads an individual track with realistic progress simulation and persistent file caching
     */
    suspend fun downloadTrack(
        track: Track,
        onProgress: ((Float) -> Unit)? = null,
        onComplete: (Track) -> Unit
    ) {
        if (_downloadingTrackIds.value.containsKey(track.id)) return

        // Update progress state
        updateTrackProgress(track.id, 0.1f)
        onProgress?.invoke(0.1f)

        val steps = listOf(0.25f, 0.55f, 0.85f, 1.0f)
        for (p in steps) {
            delay(160)
            updateTrackProgress(track.id, p)
            onProgress?.invoke(p)
        }

        // Write local offline file
        val localFile = createLocalCacheFile(track)

        // Mark downloaded
        val newSet = _downloadedTrackIds.value + track.id
        _downloadedTrackIds.value = newSet
        persistDownloadedIds(newSet)

        // Clear active progress
        val currentMap = _downloadingTrackIds.value.toMutableMap()
        currentMap.remove(track.id)
        _downloadingTrackIds.value = currentMap

        val updatedTrack = track.copy(
            isDownloaded = true,
            localUri = localFile.absolutePath
        )
        onComplete(updatedTrack)
    }

    /**
     * Downloads all undownloaded tracks in a playlist
     */
    suspend fun downloadPlaylist(
        playlist: Playlist,
        onTrackDownloaded: (Track) -> Unit,
        onPlaylistComplete: () -> Unit
    ) {
        val tracksToDownload = playlist.tracks.filter { !isTrackDownloaded(it.id) }
        if (tracksToDownload.isEmpty()) {
            onPlaylistComplete()
            return
        }

        for (track in tracksToDownload) {
            downloadTrack(track, onComplete = { updated ->
                onTrackDownloaded(updated)
            })
        }
        onPlaylistComplete()
    }

    /**
     * Deletes downloaded audio for a track and frees storage
     */
    fun deleteTrackDownload(trackId: String): Boolean {
        val file = File(downloadDir, "$trackId.dat")
        if (file.exists()) {
            file.delete()
        }
        val newSet = _downloadedTrackIds.value - trackId
        _downloadedTrackIds.value = newSet
        persistDownloadedIds(newSet)
        return true
    }

    /**
     * Deletes downloads for all tracks in a playlist
     */
    fun deletePlaylistDownloads(playlist: Playlist, onTrackUpdated: (String) -> Unit) {
        playlist.tracks.forEach { track ->
            deleteTrackDownload(track.id)
            onTrackUpdated(track.id)
        }
    }

    /**
     * Clears all downloaded tracks
     */
    fun clearAllDownloads(onCleared: () -> Unit) {
        downloadDir.listFiles()?.forEach { it.delete() }
        _downloadedTrackIds.value = emptySet()
        persistDownloadedIds(emptySet())
        onCleared()
    }

    private fun updateTrackProgress(trackId: String, progress: Float) {
        val currentMap = _downloadingTrackIds.value.toMutableMap()
        currentMap[trackId] = progress
        _downloadingTrackIds.value = currentMap
    }

    /**
     * Creates a local physical file for offline storage containing track header and metadata
     */
    private fun createLocalCacheFile(track: Track): File {
        val file = File(downloadDir, "${track.id}.dat")
        try {
            if (!file.exists()) {
                FileOutputStream(file).use { fos ->
                    // Write signature header and placeholder audio payload (~2.5 MB to 4.5 MB)
                    val header = "SHJ_OFFLINE_AUDIO_V1;ID=${track.id};TITLE=${track.title};ARTIST=${track.artist};DUR=${track.durationMs};\n"
                    fos.write(header.toByteArray())
                    // Write dummy binary chunk to represent realistic offline lossless storage
                    val dummyChunk = ByteArray(64 * 1024) { (it % 128).toByte() }
                    val iterations = (track.durationMs / 4000).toInt().coerceIn(15, 60)
                    for (i in 0 until iterations) {
                        fos.write(dummyChunk)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OfflineDownloadManager", "Failed to write local file: ${e.message}")
        }
        return file
    }

    fun getStorageStats(): StorageStats {
        var totalBytes = 0L
        downloadDir.listFiles()?.forEach {
            totalBytes += it.length()
        }
        val totalMb = totalBytes.toFloat() / (1024f * 1024f)
        val freeBytes = context.filesDir.freeSpace
        val freeMb = freeBytes.toFloat() / (1024f * 1024f)

        return StorageStats(
            downloadedCount = _downloadedTrackIds.value.size,
            totalSizeMb = totalMb,
            availableSpaceMb = freeMb
        )
    }
}
