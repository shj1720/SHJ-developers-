package com.example.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "SHJ Masters",
    val durationMs: Long = 180000L,
    val localUri: String? = null,
    val audioResName: String? = null,
    val albumArtDrawableName: String? = null,
    val albumArtUrl: String? = null,
    val genre: String = "Lo-Fi / Chill",
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val spotifyId: String? = null,
    val bpm: Int = 85
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
