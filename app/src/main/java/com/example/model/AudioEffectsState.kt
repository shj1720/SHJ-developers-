package com.example.model

data class EqualizerBand(
    val index: Int,
    val frequencyLabel: String,
    val centerFreqHz: Int,
    val gainDb: Float = 0f // -12f to +12f dB
)

enum class ReverbRoom(val displayName: String, val decaySeconds: Float, val wetMix: Float) {
    OFF("Bypass / Dry", 0.0f, 0.0f),
    STUDIO("Cozy Studio", 0.8f, 0.25f),
    LIVING_ROOM("Living Room", 1.2f, 0.35f),
    CHAMBER("Vintage Chamber", 1.8f, 0.45f),
    LARGE_HALL("Concert Hall", 2.6f, 0.55f),
    CATHEDRAL("Sacred Cathedral", 4.2f, 0.70f),
    COSMIC_SPACE("Cosmic Void", 6.0f, 0.85f)
}

enum class VintageLoFiTone(
    val displayName: String,
    val defaultCrackle: Float,
    val defaultFlutter: Float,
    val defaultWarmth: Float,
    val defaultCutoffHz: Int
) {
    CLEAN("Modern Hi-Fi", 0f, 0f, 0f, 20000),
    CASSETTE_1985("1985 Walkman Cassette", 0.35f, 0.50f, 0.65f, 9500),
    VINYL_33RPM("Vintage 33 RPM Vinyl", 0.60f, 0.25f, 0.75f, 12000),
    DUSTY_ATTIC("Dusty Tape Attic", 0.75f, 0.60f, 0.85f, 6500),
    VAPORWAVE("Dreamy Vaporwave", 0.20f, 0.85f, 0.70f, 8000),
    AM_RADIO("1950s AM Radio", 0.45f, 0.30f, 0.90f, 4000),
    CUSTOM("Custom Sound", 0.3f, 0.3f, 0.5f, 10000)
}

data class LoFiSettings(
    val isEnabled: Boolean = true,
    val activeTone: VintageLoFiTone = VintageLoFiTone.CASSETTE_1985,
    val crackleVolume: Float = 0.35f, // 0.0 to 1.0
    val tapeFlutter: Float = 0.45f,   // 0.0 to 1.0
    val analogWarmth: Float = 0.60f,  // 0.0 to 1.0
    val lowPassCutoffHz: Int = 9500   // 3000 to 20000
)

data class SoundProfilePreset(
    val id: String,
    val name: String,
    val isCustom: Boolean = false,
    val bandGainsDb: List<Float> = listOf(0f, 0f, 0f, 0f, 0f),
    val bassBoost: Float = 0f,        // 0.0 to 1.0
    val virtualizer3D: Float = 0f,    // 0.0 to 1.0
    val reverbRoom: ReverbRoom = ReverbRoom.OFF,
    val loFiPreset: VintageLoFiTone = VintageLoFiTone.CLEAN
)

object DefaultSoundPresets {
    val presets = listOf(
        SoundProfilePreset(
            id = "flat",
            name = "Flat / Pure Reference",
            bandGainsDb = listOf(0f, 0f, 0f, 0f, 0f),
            bassBoost = 0f,
            virtualizer3D = 0f,
            reverbRoom = ReverbRoom.OFF,
            loFiPreset = VintageLoFiTone.CLEAN
        ),
        SoundProfilePreset(
            id = "lofi_study",
            name = "Lo-Fi Midnight Study",
            bandGainsDb = listOf(4f, 1.5f, -1f, -2.5f, -4f),
            bassBoost = 0.55f,
            virtualizer3D = 0.40f,
            reverbRoom = ReverbRoom.STUDIO,
            loFiPreset = VintageLoFiTone.CASSETTE_1985
        ),
        SoundProfilePreset(
            id = "deep_bass",
            name = "Bass Sub-Heavy",
            bandGainsDb = listOf(8f, 5f, 1f, 0f, 1f),
            bassBoost = 0.90f,
            virtualizer3D = 0.30f,
            reverbRoom = ReverbRoom.OFF,
            loFiPreset = VintageLoFiTone.CLEAN
        ),
        SoundProfilePreset(
            id = "cyber_synth",
            name = "Cyberpunk Synthwave",
            bandGainsDb = listOf(6f, -1f, 2f, 4f, 6.5f),
            bassBoost = 0.70f,
            virtualizer3D = 0.80f,
            reverbRoom = ReverbRoom.CHAMBER,
            loFiPreset = VintageLoFiTone.VAPORWAVE
        ),
        SoundProfilePreset(
            id = "vocal_warmth",
            name = "Vocal Clarity & Warmth",
            bandGainsDb = listOf(-2f, 2f, 5.5f, 3.5f, 1f),
            bassBoost = 0.20f,
            virtualizer3D = 0.35f,
            reverbRoom = ReverbRoom.LIVING_ROOM,
            loFiPreset = VintageLoFiTone.CLEAN
        ),
        SoundProfilePreset(
            id = "acoustic_hall",
            name = "Acoustic Concert Hall",
            bandGainsDb = listOf(1f, 2f, 2.5f, 4f, 5f),
            bassBoost = 0.30f,
            virtualizer3D = 0.85f,
            reverbRoom = ReverbRoom.LARGE_HALL,
            loFiPreset = VintageLoFiTone.CLEAN
        ),
        SoundProfilePreset(
            id = "vintage_vinyl",
            name = "1970s Vinyl Cafe",
            bandGainsDb = listOf(3f, 2f, 0f, -1f, -3f),
            bassBoost = 0.45f,
            virtualizer3D = 0.25f,
            reverbRoom = ReverbRoom.STUDIO,
            loFiPreset = VintageLoFiTone.VINYL_33RPM
        )
    )
}
