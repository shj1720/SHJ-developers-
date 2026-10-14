package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SyntheticAudioEngine
import com.example.audio.VisualizerFrame
import com.example.data.CloudSyncManager
import com.example.data.FirebaseCloudSyncRepository
import com.example.data.OfflineDownloadManager
import com.example.data.StorageStats
import com.google.firebase.auth.FirebaseUser
import com.example.model.DefaultSoundPresets
import com.example.data.ExportFormat
import com.example.data.SampleMusicRepository
import com.example.data.SpotifyManager
import com.example.model.CloudSyncState
import com.example.model.EqualizerBand
import com.example.model.LoFiSettings
import com.example.model.Playlist
import com.example.model.ReverbRoom
import com.example.model.SHJTheme
import com.example.model.SoundProfilePreset
import com.example.model.Track
import com.example.model.VintageLoFiTone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class NavigationTab(val title: String) {
    PLAYER("Now Playing"),
    LIBRARY("Library"),
    EQUALIZER("Equalizer"),
    LOFI("Lo-Fi Tuner"),
    SPOTIFY("Spotify Hub"),
    CLOUD("Cloud Sync"),
    THEMES("Themes")
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val audioEngine = SyntheticAudioEngine(application)
    val spotifyManager = SpotifyManager(application)
    val cloudSyncManager = CloudSyncManager()
    val offlineDownloadManager = OfflineDownloadManager(application)
    val firebaseCloudSyncRepository = FirebaseCloudSyncRepository(application)

    // Firebase Auth & Cloud Sync State
    val firebaseUser: StateFlow<FirebaseUser?> = firebaseCloudSyncRepository.currentUser
    private val _firebaseSyncMessage = MutableStateFlow<String?>(null)
    val firebaseSyncMessage: StateFlow<String?> = _firebaseSyncMessage.asStateFlow()
    private val _isFirebaseSyncing = MutableStateFlow(false)
    val isFirebaseSyncing: StateFlow<Boolean> = _isFirebaseSyncing.asStateFlow()

    // Offline Mode & Downloads State
    val isOfflineModeForced: StateFlow<Boolean> = offlineDownloadManager.isOfflineModeForced
    val isDeviceOnline: StateFlow<Boolean> = offlineDownloadManager.isDeviceOnline
    val isEffectiveOffline: StateFlow<Boolean> = offlineDownloadManager.isEffectiveOffline
    val downloadingTrackIds: StateFlow<Map<String, Float>> = offlineDownloadManager.downloadingTrackIds
    val downloadedTrackIds: StateFlow<Set<String>> = offlineDownloadManager.downloadedTrackIds

    private val _storageStats = MutableStateFlow(offlineDownloadManager.getStorageStats())
    val storageStats: StateFlow<com.example.data.StorageStats> = _storageStats.asStateFlow()

    private val _offlineNoticeMessage = MutableStateFlow<String?>(null)
    val offlineNoticeMessage: StateFlow<String?> = _offlineNoticeMessage.asStateFlow()

    // Playback State
    val isPlaying: StateFlow<Boolean> = audioEngine.isPlaying
    val currentPositionMs: StateFlow<Long> = audioEngine.currentPositionMs
    val durationMs: StateFlow<Long> = audioEngine.durationMs
    val visualizerFrame: StateFlow<VisualizerFrame> = audioEngine.visualizerFrame

    // Initialize tracks synced with persisted downloads
    private val initialTracks = SampleMusicRepository.builtInTracks.map { track ->
        track.copy(isDownloaded = offlineDownloadManager.isTrackDownloaded(track.id))
    }

    private val initialPlaylists = SampleMusicRepository.defaultPlaylists.map { pl ->
        pl.copy(tracks = pl.tracks.map { track ->
            track.copy(isDownloaded = offlineDownloadManager.isTrackDownloaded(track.id))
        })
    }

    private val _currentTrack = MutableStateFlow(initialTracks.first())
    val currentTrack: StateFlow<Track> = _currentTrack.asStateFlow()

    private val _currentQueue = MutableStateFlow(initialTracks)
    val currentQueue: StateFlow<List<Track>> = _currentQueue.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<Playlist?>(initialPlaylists.first())
    val currentPlaylist: StateFlow<Playlist?> = _currentPlaylist.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    // Navigation
    private val _activeTab = MutableStateFlow(NavigationTab.PLAYER)
    val activeTab: StateFlow<NavigationTab> = _activeTab.asStateFlow()

    // Playlists & Tracks
    private val _playlists = MutableStateFlow(initialPlaylists)
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _allTracks = MutableStateFlow(initialTracks)
    val allTracks: StateFlow<List<Track>> = _allTracks.asStateFlow()

    // Theme
    private val _currentTheme = MutableStateFlow(SHJTheme.CYBERPUNK_NEON)
    val currentTheme: StateFlow<SHJTheme> = _currentTheme.asStateFlow()

    // Equalizer & Audio Profiles
    val defaultBands = listOf(
        EqualizerBand(0, "60 Hz", 60, 4f),
        EqualizerBand(1, "230 Hz", 230, 1.5f),
        EqualizerBand(2, "910 Hz", 910, -1f),
        EqualizerBand(3, "3.6 kHz", 3600, -2f),
        EqualizerBand(4, "14 kHz", 14000, -3.5f)
    )
    private val _eqBands = MutableStateFlow(defaultBands)
    val eqBands: StateFlow<List<EqualizerBand>> = _eqBands.asStateFlow()

    private val _bassBoost = MutableStateFlow(0.65f)
    val bassBoost: StateFlow<Float> = _bassBoost.asStateFlow()

    private val _virtualizer = MutableStateFlow(0.45f)
    val virtualizer: StateFlow<Float> = _virtualizer.asStateFlow()

    private val _reverbRoom = MutableStateFlow(ReverbRoom.STUDIO)
    val reverbRoom: StateFlow<ReverbRoom> = _reverbRoom.asStateFlow()

    private val _soundPresets = MutableStateFlow(DefaultSoundPresets.presets)
    val soundPresets: StateFlow<List<SoundProfilePreset>> = _soundPresets.asStateFlow()

    private val _activePreset = MutableStateFlow(DefaultSoundPresets.presets[1]) // Lo-Fi Study default
    val activePreset: StateFlow<SoundProfilePreset> = _activePreset.asStateFlow()

    // Lo-Fi Tuner State
    private val _loFiSettings = MutableStateFlow(
        LoFiSettings(
            isEnabled = true,
            activeTone = VintageLoFiTone.CASSETTE_1985,
            crackleVolume = 0.40f,
            tapeFlutter = 0.50f,
            analogWarmth = 0.65f,
            lowPassCutoffHz = 9500
        )
    )
    val loFiSettings: StateFlow<LoFiSettings> = _loFiSettings.asStateFlow()

    // Cloud Sync State
    val cloudSyncState: StateFlow<CloudSyncState> = cloudSyncManager.syncState

    init {
        audioEngine.setCompletionListener {
            playNextTrack()
        }
        applyCurrentAudioProfile()
    }

    fun setActiveTab(tab: NavigationTab) {
        _activeTab.value = tab
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun setTheme(theme: SHJTheme) {
        _currentTheme.value = theme
    }

    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        if (isEffectiveOffline.value && !track.isDownloaded) {
            _offlineNoticeMessage.value = "Offline Mode is active. \"${track.title}\" is not downloaded. Download it or disable Offline Mode to stream."
            return
        }
        _offlineNoticeMessage.value = null
        _currentTrack.value = track
        newQueue?.let { _currentQueue.value = it }
        audioEngine.playTrack(track)
    }

    fun playPlaylist(playlist: Playlist, startIndex: Int = 0) {
        if (playlist.tracks.isEmpty()) return
        val availableTracks = if (isEffectiveOffline.value) playlist.tracks.filter { it.isDownloaded } else playlist.tracks
        if (availableTracks.isEmpty()) {
            _offlineNoticeMessage.value = "Playlist \"${playlist.name}\" has no downloaded tracks for offline playback. Tap download to save them."
            return
        }
        _currentPlaylist.value = playlist
        _currentQueue.value = availableTracks
        val trackToPlay = availableTracks.getOrElse(startIndex) { availableTracks.first() }
        playTrack(trackToPlay, availableTracks)
    }

    fun togglePlayPause() {
        if (isPlaying.value) {
            audioEngine.pause()
        } else {
            audioEngine.resume()
        }
    }

    fun playNextTrack() {
        val rawQueue = _currentQueue.value
        val queue = if (isEffectiveOffline.value) rawQueue.filter { it.isDownloaded } else rawQueue
        if (queue.isEmpty()) {
            _offlineNoticeMessage.value = "No downloaded tracks available in current queue."
            return
        }
        val currentIdx = queue.indexOfFirst { it.id == _currentTrack.value.id }

        if (_repeatMode.value == RepeatMode.ONE) {
            audioEngine.seekTo(0)
            audioEngine.resume()
            return
        }

        val nextIdx = if (_isShuffle.value) {
            (queue.indices).random()
        } else {
            if (currentIdx == -1 || currentIdx >= queue.size - 1) {
                if (_repeatMode.value == RepeatMode.ALL) 0 else return
            } else {
                currentIdx + 1
            }
        }
        val nextTrack = queue[nextIdx]
        playTrack(nextTrack)
    }

    fun playPreviousTrack() {
        val rawQueue = _currentQueue.value
        val queue = if (isEffectiveOffline.value) rawQueue.filter { it.isDownloaded } else rawQueue
        if (queue.isEmpty()) return
        val currentIdx = queue.indexOfFirst { it.id == _currentTrack.value.id }

        if (currentPositionMs.value > 3000L) {
            audioEngine.seekTo(0)
            return
        }

        val prevIdx = if (currentIdx <= 0) queue.size - 1 else currentIdx - 1
        val prevTrack = queue[prevIdx]
        playTrack(prevTrack)
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    // Offline Mode & Downloads Actions
    fun toggleOfflineMode(): Boolean {
        val result = offlineDownloadManager.toggleOfflineMode()
        if (result && !_currentTrack.value.isDownloaded) {
            val firstDownloaded = _allTracks.value.firstOrNull { it.isDownloaded }
            if (firstDownloaded != null) {
                playTrack(firstDownloaded)
            }
        }
        return result
    }

    fun setOfflineModeForced(forced: Boolean) {
        offlineDownloadManager.setOfflineModeForced(forced)
    }

    fun dismissOfflineNotice() {
        _offlineNoticeMessage.value = null
    }

    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            offlineDownloadManager.downloadTrack(track) { updatedTrack: Track ->
                updateTrackDownloadStatus(updatedTrack.id, isDownloaded = true, localUri = updatedTrack.localUri)
                _storageStats.value = offlineDownloadManager.getStorageStats()
            }
        }
    }

    fun deleteDownloadedTrack(track: Track) {
        offlineDownloadManager.deleteTrackDownload(track.id)
        updateTrackDownloadStatus(track.id, isDownloaded = false, localUri = null)
        _storageStats.value = offlineDownloadManager.getStorageStats()
    }

    fun downloadPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            offlineDownloadManager.downloadPlaylist(
                playlist = playlist,
                onTrackDownloaded = { updatedTrack: Track ->
                    updateTrackDownloadStatus(updatedTrack.id, isDownloaded = true, localUri = updatedTrack.localUri)
                    _storageStats.value = offlineDownloadManager.getStorageStats()
                },
                onPlaylistComplete = {
                    _storageStats.value = offlineDownloadManager.getStorageStats()
                }
            )
        }
    }

    fun deleteDownloadedPlaylist(playlist: Playlist) {
        offlineDownloadManager.deletePlaylistDownloads(playlist) { trackId: String ->
            updateTrackDownloadStatus(trackId, isDownloaded = false, localUri = null)
        }
        _storageStats.value = offlineDownloadManager.getStorageStats()
    }

    fun downloadSelectedTracks(tracks: List<Track>) {
        viewModelScope.launch {
            tracks.filter { !it.isDownloaded }.forEach { track ->
                offlineDownloadManager.downloadTrack(track) { updatedTrack: Track ->
                    updateTrackDownloadStatus(updatedTrack.id, isDownloaded = true, localUri = updatedTrack.localUri)
                }
            }
            _storageStats.value = offlineDownloadManager.getStorageStats()
        }
    }

    fun deleteSelectedTracks(tracks: List<Track>) {
        tracks.filter { it.isDownloaded }.forEach { track ->
            offlineDownloadManager.deleteTrackDownload(track.id)
            updateTrackDownloadStatus(track.id, isDownloaded = false, localUri = null)
        }
        _storageStats.value = offlineDownloadManager.getStorageStats()
    }

    fun clearAllDownloads() {
        offlineDownloadManager.clearAllDownloads {
            _allTracks.value = _allTracks.value.map { it.copy(isDownloaded = false, localUri = null) }
            _playlists.value = _playlists.value.map { pl ->
                pl.copy(tracks = pl.tracks.map { it.copy(isDownloaded = false, localUri = null) })
            }
            _currentTrack.value = _currentTrack.value.copy(isDownloaded = false, localUri = null)
            _storageStats.value = offlineDownloadManager.getStorageStats()
        }
    }

    fun refreshStorageStats() {
        _storageStats.value = offlineDownloadManager.getStorageStats()
    }

    private fun updateTrackDownloadStatus(trackId: String, isDownloaded: Boolean, localUri: String?) {
        _allTracks.value = _allTracks.value.map {
            if (it.id == trackId) it.copy(isDownloaded = isDownloaded, localUri = localUri) else it
        }
        _playlists.value = _playlists.value.map { pl ->
            pl.copy(tracks = pl.tracks.map {
                if (it.id == trackId) it.copy(isDownloaded = isDownloaded, localUri = localUri) else it
            })
        }
        _currentQueue.value = _currentQueue.value.map {
            if (it.id == trackId) it.copy(isDownloaded = isDownloaded, localUri = localUri) else it
        }
        if (_currentTrack.value.id == trackId) {
            _currentTrack.value = _currentTrack.value.copy(isDownloaded = isDownloaded, localUri = localUri)
        }
    }

    fun toggleFavorite(track: Track) {
        val updatedTrack = track.copy(isFavorite = !track.isFavorite)
        if (_currentTrack.value.id == track.id) {
            _currentTrack.value = updatedTrack
        }
        _allTracks.value = _allTracks.value.map { if (it.id == track.id) updatedTrack else it }
        _playlists.value = _playlists.value.map { pl ->
            pl.copy(tracks = pl.tracks.map { if (it.id == track.id) updatedTrack else it })
        }
    }

    // Equalizer & Audio Presets
    fun updateEqBand(index: Int, gainDb: Float) {
        val updated = _eqBands.value.toMutableList()
        if (index in updated.indices) {
            updated[index] = updated[index].copy(gainDb = gainDb)
            _eqBands.value = updated
            audioEngine.applyEqualizerBands(updated.map { it.gainDb })
        }
    }

    fun setBassBoost(level: Float) {
        _bassBoost.value = level
        audioEngine.applyBassBoost(level)
    }

    fun setVirtualizer(level: Float) {
        _virtualizer.value = level
        audioEngine.applyVirtualizer(level)
    }

    fun setReverbRoom(room: ReverbRoom) {
        _reverbRoom.value = room
        audioEngine.applyReverb(room)
    }

    fun selectPreset(preset: SoundProfilePreset) {
        _activePreset.value = preset
        val bands = _eqBands.value.mapIndexed { idx, band ->
            band.copy(gainDb = preset.bandGainsDb.getOrElse(idx) { 0f })
        }
        _eqBands.value = bands
        _bassBoost.value = preset.bassBoost
        _virtualizer.value = preset.virtualizer3D
        _reverbRoom.value = preset.reverbRoom

        if (preset.loFiPreset != VintageLoFiTone.CLEAN) {
            _loFiSettings.value = _loFiSettings.value.copy(
                isEnabled = true,
                activeTone = preset.loFiPreset,
                crackleVolume = preset.loFiPreset.defaultCrackle,
                tapeFlutter = preset.loFiPreset.defaultFlutter,
                analogWarmth = preset.loFiPreset.defaultWarmth,
                lowPassCutoffHz = preset.loFiPreset.defaultCutoffHz
            )
        }

        applyCurrentAudioProfile()
    }

    fun saveCustomPreset(name: String) {
        val custom = SoundProfilePreset(
            id = "custom_${UUID.randomUUID().toString().take(6)}",
            name = name,
            isCustom = true,
            bandGainsDb = _eqBands.value.map { it.gainDb },
            bassBoost = _bassBoost.value,
            virtualizer3D = _virtualizer.value,
            reverbRoom = _reverbRoom.value,
            loFiPreset = _loFiSettings.value.activeTone
        )
        _soundPresets.value = _soundPresets.value + custom
        _activePreset.value = custom
    }

    private fun applyCurrentAudioProfile() {
        audioEngine.applyEqualizerBands(_eqBands.value.map { it.gainDb })
        audioEngine.applyBassBoost(_bassBoost.value)
        audioEngine.applyVirtualizer(_virtualizer.value)
        audioEngine.applyReverb(_reverbRoom.value)
        audioEngine.applyLoFiSettings(_loFiSettings.value)
    }

    // Lo-Fi Tuner Controls
    fun setLoFiEnabled(enabled: Boolean) {
        _loFiSettings.value = _loFiSettings.value.copy(isEnabled = enabled)
        audioEngine.applyLoFiSettings(_loFiSettings.value)
    }

    fun setLoFiTone(tone: VintageLoFiTone) {
        _loFiSettings.value = _loFiSettings.value.copy(
            activeTone = tone,
            crackleVolume = tone.defaultCrackle,
            tapeFlutter = tone.defaultFlutter,
            analogWarmth = tone.defaultWarmth,
            lowPassCutoffHz = tone.defaultCutoffHz
        )
        audioEngine.applyLoFiSettings(_loFiSettings.value)
    }

    fun updateLoFiCrackle(level: Float) {
        _loFiSettings.value = _loFiSettings.value.copy(crackleVolume = level)
        audioEngine.applyLoFiSettings(_loFiSettings.value)
    }

    fun updateLoFiFlutter(level: Float) {
        _loFiSettings.value = _loFiSettings.value.copy(tapeFlutter = level)
        audioEngine.applyLoFiSettings(_loFiSettings.value)
    }

    fun updateLoFiWarmth(level: Float) {
        _loFiSettings.value = _loFiSettings.value.copy(analogWarmth = level)
        audioEngine.applyLoFiSettings(_loFiSettings.value)
    }

    fun updateLoFiCutoff(hz: Int) {
        _loFiSettings.value = _loFiSettings.value.copy(lowPassCutoffHz = hz)
        audioEngine.applyLoFiSettings(_loFiSettings.value)
    }

    // Spotify Hub Actions
    fun importSpotifyPlaylist(link: String) {
        val imported = spotifyManager.parseSpotifyPlaylistLink(link)
        _playlists.value = _playlists.value + imported
        _allTracks.value = _allTracks.value + imported.tracks
    }

    fun exportPlaylist(playlist: Playlist, format: ExportFormat): String {
        return spotifyManager.exportPlaylist(playlist, format)
    }

    fun downloadAllPlaylistTracks(playlist: Playlist) {
        viewModelScope.launch {
            spotifyManager.downloadPlaylistBatch(playlist) { downloadedTrack ->
                // Mark track as downloaded in memory and persist in offline storage
                updateTrackDownloadStatus(downloadedTrack.id, isDownloaded = true, localUri = downloadedTrack.localUri)
                viewModelScope.launch {
                    offlineDownloadManager.downloadTrack(downloadedTrack) { _: Track ->
                        _storageStats.value = offlineDownloadManager.getStorageStats()
                    }
                }
            }
        }
    }

    // Cloud Sync Actions
    fun pushPlaylistsToCloud() {
        viewModelScope.launch {
            cloudSyncManager.pushToCloud(_playlists.value)
        }
    }

    fun pullPlaylistsFromCloud(remoteSyncCode: String) {
        viewModelScope.launch {
            val updated = cloudSyncManager.pullFromCloud(remoteSyncCode, _playlists.value)
            _playlists.value = updated
        }
    }

    fun generateNewCloudSyncCode() {
        cloudSyncManager.generateNewSyncCode()
    }

    // Firebase Authentication & Cloud Database Sync Actions
    fun signInWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isFirebaseSyncing.value = true
            val result = firebaseCloudSyncRepository.signInWithGoogleCredential(idToken)
            _isFirebaseSyncing.value = false
            if (result.isSuccess) {
                _firebaseSyncMessage.value = "Signed in as ${result.getOrNull()?.displayName ?: "Google User"}"
                onResult(true, null)
                pushPlaylistsToFirebase()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Google Sign-in failed"
                _firebaseSyncMessage.value = "Sign-in error: $err"
                onResult(false, err)
            }
        }
    }

    fun signOutFirebase() {
        firebaseCloudSyncRepository.signOut()
        _firebaseSyncMessage.value = "Signed out from Google Cloud"
    }

    fun dismissFirebaseSyncMessage() {
        _firebaseSyncMessage.value = null
    }

    fun pushPlaylistsToFirebase() {
        viewModelScope.launch {
            _isFirebaseSyncing.value = true
            val result = firebaseCloudSyncRepository.pushPlaylists(_playlists.value)
            _isFirebaseSyncing.value = false
            if (result.isSuccess) {
                _firebaseSyncMessage.value = "Successfully backed up ${_playlists.value.size} playlists to Firestore!"
                cloudSyncManager.pushToCloud(_playlists.value)
            } else {
                _firebaseSyncMessage.value = "Firestore backup failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun pullPlaylistsFromFirebase() {
        viewModelScope.launch {
            _isFirebaseSyncing.value = true
            val result = firebaseCloudSyncRepository.pullPlaylists()
            _isFirebaseSyncing.value = false
            if (result.isSuccess) {
                val remotePlaylists = result.getOrNull().orEmpty()
                if (remotePlaylists.isNotEmpty()) {
                    val merged = _playlists.value.toMutableList()
                    remotePlaylists.forEach { pl ->
                        if (merged.none { it.id == pl.id }) {
                            merged.add(pl)
                        }
                    }
                    _playlists.value = merged
                    val allTr = _allTracks.value.toMutableList()
                    remotePlaylists.flatMap { it.tracks }.forEach { tr ->
                        if (allTr.none { it.id == tr.id }) {
                            allTr.add(tr)
                        }
                    }
                    _allTracks.value = allTr
                    _firebaseSyncMessage.value = "Restored ${remotePlaylists.size} playlists from Firestore!"
                } else {
                    _firebaseSyncMessage.value = "No remote playlists found in cloud. Try backing up first."
                }
            } else {
                _firebaseSyncMessage.value = "Firestore restore failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stop()
    }
}
