package com.example.model

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val coverDrawableName: String? = null,
    val tracks: List<Track> = emptyList(),
    val isSpotifyImport: Boolean = false,
    val spotifyUrl: String? = null,
    val isSyncedToCloud: Boolean = true,
    val lastModified: Long = System.currentTimeMillis()
) {
    val totalDurationMs: Long
        get() = tracks.sumOf { it.durationMs }

    val formattedTotalDuration: String
        get() {
            val totalSeconds = totalDurationMs / 1000
            val minutes = totalSeconds / 60
            val hours = minutes / 60
            val remainingMins = minutes % 60
            return if (hours > 0) "%d hr %d min".format(hours, remainingMins)
            else "%d mins".format(minutes)
        }

    val downloadedTracksCount: Int
        get() = tracks.count { it.isDownloaded }

    val isFullyDownloaded: Boolean
        get() = tracks.isNotEmpty() && tracks.all { it.isDownloaded }

    val isPartiallyDownloaded: Boolean
        get() = tracks.any { it.isDownloaded } && !isFullyDownloaded
}
