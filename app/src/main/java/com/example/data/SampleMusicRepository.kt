package com.example.data

import com.example.model.Playlist
import com.example.model.Track

object SampleMusicRepository {

    val builtInTracks = listOf(
        Track(
            id = "track_lofi_01",
            title = "Midnight Rain & Tape Hiss",
            artist = "Aura Bloom",
            album = "Lo-Fi Chronicles Vol. 1",
            durationMs = 194000L,
            albumArtDrawableName = "art_lofi_study",
            genre = "Lo-Fi Hip-Hop / Chill",
            isFavorite = true,
            isDownloaded = true,
            bpm = 82
        ),
        Track(
            id = "track_synth_02",
            title = "Cyber Neon Expressway",
            artist = "Vector Waves",
            album = "Outrun Odyssey",
            durationMs = 228000L,
            albumArtDrawableName = "art_synthwave_sunset",
            genre = "Synthwave / Retrowave",
            isFavorite = true,
            isDownloaded = true,
            bpm = 118
        ),
        Track(
            id = "track_vinyl_03",
            title = "Coffee, Books & 33 RPM",
            artist = "Warm Velvet",
            album = "Analog Dusty Cafe",
            durationMs = 175000L,
            albumArtDrawableName = "art_vinyl_disc",
            genre = "Lo-Fi Jazzhop",
            isFavorite = false,
            isDownloaded = true,
            bpm = 76
        ),
        Track(
            id = "track_lofi_04",
            title = "Tokyo Twilight Train",
            artist = "Shibuya Sound Lab",
            album = "Night Transit",
            durationMs = 210000L,
            albumArtDrawableName = "art_lofi_study",
            genre = "Lo-Fi Ambient",
            isFavorite = true,
            isDownloaded = false,
            bpm = 84
        ),
        Track(
            id = "track_synth_05",
            title = "Horizon Overdrive",
            artist = "Kavinsky Reverie",
            album = "Outrun Odyssey",
            durationMs = 245000L,
            albumArtDrawableName = "art_synthwave_sunset",
            genre = "Synthwave",
            isFavorite = false,
            isDownloaded = false,
            bpm = 124
        ),
        Track(
            id = "track_vinyl_06",
            title = "Acoustic Fireplace & Rain",
            artist = "Echo Grove",
            album = "Autumn Memories",
            durationMs = 168000L,
            albumArtDrawableName = "art_vinyl_disc",
            genre = "Acoustic Lo-Fi",
            isFavorite = true,
            isDownloaded = true,
            bpm = 70
        ),
        Track(
            id = "track_lofi_07",
            title = "Sleepy Cat on the Keyboard",
            artist = "Purr Beats",
            album = "Lo-Fi Chronicles Vol. 1",
            durationMs = 182000L,
            albumArtDrawableName = "art_lofi_study",
            genre = "Lo-Fi Chill",
            isFavorite = false,
            isDownloaded = false,
            bpm = 80
        ),
        Track(
            id = "track_synth_08",
            title = "Starlight Grid Navigator",
            artist = "Vector Waves",
            album = "Neon Metropolis",
            durationMs = 215000L,
            albumArtDrawableName = "art_synthwave_sunset",
            genre = "Synthwave / Cyberpunk",
            isFavorite = false,
            isDownloaded = false,
            bpm = 120
        )
    )

    val defaultPlaylists = listOf(
        Playlist(
            id = "pl_lofi_study",
            name = "Cozy Lo-Fi & Study Beats",
            description = "Gentle soothing beats with tape warmth and soft rain for deep focus.",
            coverDrawableName = "art_lofi_study",
            tracks = listOf(
                builtInTracks[0],
                builtInTracks[2],
                builtInTracks[3],
                builtInTracks[5],
                builtInTracks[6]
            ),
            isSpotifyImport = false,
            isSyncedToCloud = true
        ),
        Playlist(
            id = "pl_synth_night",
            name = "Neon Night Driver 1986",
            description = "High-octane analog synthesizers, punchy drums and retro futuristic highways.",
            coverDrawableName = "art_synthwave_sunset",
            tracks = listOf(
                builtInTracks[1],
                builtInTracks[4],
                builtInTracks[7]
            ),
            isSpotifyImport = false,
            isSyncedToCloud = true
        ),
        Playlist(
            id = "pl_vinyl_sessions",
            name = "Dusty Vinyl Sessions",
            description = "Warm crackling 33 RPM jazz chords and vintage Rhodes piano grooves.",
            coverDrawableName = "art_vinyl_disc",
            tracks = listOf(
                builtInTracks[2],
                builtInTracks[0],
                builtInTracks[5]
            ),
            isSpotifyImport = false,
            isSyncedToCloud = true
        )
    )

    val spotifyCuratedPresets = listOf(
        Playlist(
            id = "sp_lofi_beats",
            name = "Spotify: Lofi Beats to Relax / Study",
            description = "Official Spotify Curated • 2.8M Saves • Perfect chill study soundtrack",
            coverDrawableName = "art_lofi_study",
            spotifyUrl = "https://open.spotify.com/playlist/37i9dQZF1DXdLEN7aqioXM",
            isSpotifyImport = true,
            tracks = listOf(
                Track("sp_tr_01", "Snowman (Chill Lofi)", "Wun Two, Lofi Fruits", "Winter Chill", 172000L, albumArtDrawableName = "art_lofi_study", genre = "Lo-Fi", bpm = 78),
                Track("sp_tr_02", "Warm Coffee In Bed", "Kavv, Kupla", "Bedroom Lofi", 195000L, albumArtDrawableName = "art_lofi_study", genre = "Lo-Fi", bpm = 82),
                Track("sp_tr_03", "Midnight Solitude", "Idealism", "Hiraeth EP", 154000L, albumArtDrawableName = "art_lofi_study", genre = "Lo-Fi", bpm = 74),
                Track("sp_tr_04", "Raindrops on the Window", "Jinsang", "Life", 168000L, albumArtDrawableName = "art_lofi_study", genre = "Lo-Fi", bpm = 80),
                Track("sp_tr_05", "Tokyo Library Walk", "Tomppabeats", "Harbor", 143000L, albumArtDrawableName = "art_lofi_study", genre = "Lo-Fi", bpm = 85),
                Track("sp_tr_06", "Affection", "Joji (Lofi Re-edit)", "In Tongues", 188000L, albumArtDrawableName = "art_lofi_study", genre = "Lo-Fi", bpm = 79)
            )
        ),
        Playlist(
            id = "sp_synthwave_drive",
            name = "Spotify: Synthwave From Outer Space",
            description = "Official Spotify Curated • 1.4M Saves • Retrowave, DarkSynth, Cyberpunk",
            coverDrawableName = "art_synthwave_sunset",
            spotifyUrl = "https://open.spotify.com/playlist/37i9dQZF1DXdLEN7aqioXN",
            isSpotifyImport = true,
            tracks = listOf(
                Track("sp_tr_07", "Nightcall (Remaster)", "Kavinsky", "OutRun", 259000L, albumArtDrawableName = "art_synthwave_sunset", genre = "Synthwave", bpm = 115),
                Track("sp_tr_08", "Tech Noir", "Gunship", "Gunship", 298000L, albumArtDrawableName = "art_synthwave_sunset", genre = "Synthwave", bpm = 120),
                Track("sp_tr_09", "Resonance", "HOME", "Odyssey", 212000L, albumArtDrawableName = "art_synthwave_sunset", genre = "Chillwave", bpm = 105),
                Track("sp_tr_10", "Turbo Killer", "Carpenter Brut", "Trilogy", 208000L, albumArtDrawableName = "art_synthwave_sunset", genre = "Darksynth", bpm = 135),
                Track("sp_tr_11", "Days of Thunder", "The Midnight", "Days of Thunder", 289000L, albumArtDrawableName = "art_synthwave_sunset", genre = "Synthwave", bpm = 112)
            )
        )
    )
}
