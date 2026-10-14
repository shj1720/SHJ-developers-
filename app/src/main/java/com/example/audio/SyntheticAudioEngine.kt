package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.util.Log
import com.example.model.LoFiSettings
import com.example.model.ReverbRoom
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class SyntheticAudioEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private var audioTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private var nativeEqualizer: Equalizer? = null
    private var nativeBassBoost: BassBoost? = null
    private var nativeReverb: PresetReverb? = null
    private var nativeVirtualizer: Virtualizer? = null

    private var playbackJob: Job? = null
    private var isPlayingInternal = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(180000L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _visualizerFrame = MutableStateFlow(VisualizerFrame())
    val visualizerFrame: StateFlow<VisualizerFrame> = _visualizerFrame.asStateFlow()

    private var currentTrack: Track? = null
    private var onTrackCompleted: (() -> Unit)? = null

    // Audio effects parameters
    var eqBandGains: List<Float> = listOf(0f, 0f, 0f, 0f, 0f)
    var bassBoostAmount: Float = 0f
    var virtualizerAmount: Float = 0f
    var reverbRoom: ReverbRoom = ReverbRoom.OFF
    var lofiSettings: LoFiSettings = LoFiSettings()

    fun setCompletionListener(listener: () -> Unit) {
        onTrackCompleted = listener
    }

    fun playTrack(track: Track, startPositionMs: Long = 0L) {
        stop()
        currentTrack = track
        _durationMs.value = track.durationMs
        _currentPositionMs.value = startPositionMs

        if (!track.localUri.isNullOrEmpty()) {
            playLocalUri(track.localUri, startPositionMs)
        } else {
            playSyntheticAudio(track, startPositionMs)
        }
    }

    private fun playLocalUri(uriString: String, startPositionMs: Long) {
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.parse(uriString))
                prepare()
                seekTo(startPositionMs.toInt())
                start()
            }
            mediaPlayer = player
            attachNativeEffects(player.audioSessionId)

            _isPlaying.value = true
            isPlayingInternal = true

            player.setOnCompletionListener {
                _isPlaying.value = false
                isPlayingInternal = false
                onTrackCompleted?.invoke()
            }

            startPositionTrackingLoop()
        } catch (e: Exception) {
            Log.e("SHJAudioEngine", "Error playing local URI, fallback to synth", e)
            currentTrack?.let { playSyntheticAudio(it, startPositionMs) }
        }
    }

    private fun playSyntheticAudio(track: Track, startPositionMs: Long) {
        isPlayingInternal = true
        _isPlaying.value = true

        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        try {
            val trackAudio = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = trackAudio
            trackAudio.play()
            attachNativeEffects(trackAudio.audioSessionId)
        } catch (e: Exception) {
            Log.e("SHJAudioEngine", "AudioTrack init error: ${e.message}")
        }

        playbackJob = scope.launch {
            val random = Random()
            val chunkFrames = 2048
            val pcmBuffer = ShortArray(chunkFrames * 2)

            // Musical chords based on track genre or bpm
            val chordProgressions = when {
                track.genre.contains("Synth", ignoreCase = true) -> listOf(
                    doubleArrayOf(130.81, 196.00, 246.94, 293.66), // C3, G3, B3, D4
                    doubleArrayOf(110.00, 164.81, 220.00, 261.63), // A2, E3, A3, C4
                    doubleArrayOf(87.31, 130.81, 174.61, 220.00),  // F2, C3, F3, A3
                    doubleArrayOf(98.00, 146.83, 196.00, 246.94)   // G2, D3, G3, B3
                )
                track.genre.contains("Jazz", ignoreCase = true) -> listOf(
                    doubleArrayOf(146.83, 174.61, 220.00, 261.63), // Dm7
                    doubleArrayOf(196.00, 246.94, 293.66, 349.23), // G7
                    doubleArrayOf(130.81, 164.81, 196.00, 246.94), // Cmaj7
                    doubleArrayOf(110.00, 130.81, 164.81, 196.00)  // Am7
                )
                else -> listOf(
                    doubleArrayOf(146.83, 220.00, 277.18, 329.63), // Dm9
                    doubleArrayOf(116.54, 174.61, 233.08, 293.66), // Bbmaj7
                    doubleArrayOf(130.81, 196.00, 246.94, 329.63), // Cmaj9
                    doubleArrayOf(110.00, 164.81, 220.00, 261.63)  // Am7
                )
            }

            var sampleIndex = (startPositionMs * sampleRate / 1000L)
            var currentPosMs = startPositionMs
            val totalTrackMs = track.durationMs

            // Visualizer spectrum values
            val visualizerFreqs = FloatArray(32) { 0.1f }

            while (isActive && isPlayingInternal && currentPosMs < totalTrackMs) {
                val bpm = track.bpm.coerceIn(60, 140)
                val beatSamples = (sampleRate * 60.0 / bpm).toInt()
                val measureSamples = beatSamples * 4

                val chordIdx = ((sampleIndex / (measureSamples * 2)) % chordProgressions.size).toInt()
                val currentChord = chordProgressions[chordIdx]

                // Lo-Fi effect parameters
                val crackleAmp = if (lofiSettings.isEnabled) lofiSettings.crackleVolume * 1600f else 0f
                val flutterMod = if (lofiSettings.isEnabled) lofiSettings.tapeFlutter * 0.04 else 0.0
                val warmthGain = if (lofiSettings.isEnabled) (1.0 + lofiSettings.analogWarmth * 0.4) else 1.0

                // Equalizer gains
                val bassEqGain = 1.0 + (eqBandGains.getOrElse(0) { 0f } / 12f) * 0.6 + (bassBoostAmount * 0.8)
                val midEqGain = 1.0 + (eqBandGains.getOrElse(2) { 0f } / 12f) * 0.5
                val trebleEqGain = 1.0 + (eqBandGains.getOrElse(4) { 0f } / 12f) * 0.5

                var peakAmp = 0f

                for (i in 0 until chunkFrames) {
                    val t = (sampleIndex + i).toDouble() / sampleRate
                    // Tape flutter wow modulation
                    val pitchDrift = sin(t * 1.3) * flutterMod + sin(t * 0.3) * (flutterMod * 0.6)

                    // Chord synthesis (sine with subtle harmonic warmth)
                    var chordSum = 0.0
                    for (freq in currentChord) {
                        val modulatedFreq = freq * (1.0 + pitchDrift)
                        val noteWave = sin(2.0 * PI * modulatedFreq * t)
                        val harmonic = sin(4.0 * PI * modulatedFreq * t) * 0.25 * warmthGain
                        chordSum += (noteWave + harmonic)
                    }
                    chordSum = (chordSum / currentChord.size) * midEqGain

                    // Beat & rhythm bass pulse
                    val beatPos = ((sampleIndex + i) % beatSamples).toDouble() / beatSamples
                    val kickEnv = (1.0 - beatPos).coerceIn(0.0, 1.0)
                    val kick = if (beatPos < 0.25) sin(2.0 * PI * 55.0 * t) * (kickEnv * kickEnv) * 1.8 * bassEqGain else 0.0

                    // Hi-hat / shaker tap on eighth notes
                    val eighthPos = ((sampleIndex + i) % (beatSamples / 2)).toDouble() / (beatSamples / 2)
                    val hat = if (eighthPos < 0.08) (random.nextDouble() * 2.0 - 1.0) * (1.0 - eighthPos / 0.08) * 0.25 * trebleEqGain else 0.0

                    // Vinyl surface crackle burst
                    val crackle = if (crackleAmp > 0 && random.nextFloat() < 0.003f) {
                        (random.nextFloat() * 2f - 1f) * crackleAmp
                    } else 0f

                    // Master mix
                    var mixedSample = (chordSum * 7500.0 + kick * 9000.0 + hat * 5000.0 + crackle)
                    // Soft clipping / tube saturation
                    if (mixedSample > 28000.0) mixedSample = 28000.0
                    else if (mixedSample < -28000.0) mixedSample = -28000.0

                    val sampleShort = mixedSample.toInt().toShort()

                    // Stereo separation & 3D virtualizer
                    val stereoWidth = 0.5f + (virtualizerAmount * 0.5f)
                    val left = (sampleShort * (1.0 + stereoWidth * 0.2)).toInt().coerceIn(-32767, 32767).toShort()
                    val right = (sampleShort * (1.0 - stereoWidth * 0.2)).toInt().coerceIn(-32767, 32767).toShort()

                    pcmBuffer[i * 2] = left
                    pcmBuffer[i * 2 + 1] = right

                    val absSample = abs(sampleShort.toFloat()) / 32768f
                    if (absSample > peakAmp) peakAmp = absSample
                }

                audioTrack?.write(pcmBuffer, 0, pcmBuffer.size)

                sampleIndex += chunkFrames
                currentPosMs = (sampleIndex * 1000L / sampleRate)
                _currentPositionMs.value = currentPosMs

                // Generate real-time responsive visualizer spectrum bars
                val beatProgress = (currentPosMs % (60000 / bpm)).toFloat() / (60000 / bpm)
                val pulse = (1f - beatProgress).coerceIn(0.2f, 1f)

                for (b in 0 until 32) {
                    val base = 0.15f + 0.7f * pulse * (1f - (b / 40f)) + random.nextFloat() * 0.15f
                    val eqEffect = when {
                        b < 8 -> (bassEqGain.toFloat() * 0.5f).coerceIn(0.2f, 1.2f)
                        b < 20 -> (midEqGain.toFloat() * 0.5f).coerceIn(0.2f, 1.0f)
                        else -> (trebleEqGain.toFloat() * 0.5f).coerceIn(0.1f, 1.0f)
                    }
                    visualizerFreqs[b] = (base * eqEffect).coerceIn(0.05f, 1.0f)
                }
                _visualizerFrame.value = VisualizerFrame(visualizerFreqs.clone(), peakAmp.coerceIn(0.1f, 1.0f))
            }

            if (currentPosMs >= totalTrackMs) {
                _isPlaying.value = false
                isPlayingInternal = false
                onTrackCompleted?.invoke()
            }
        }
    }

    private fun startPositionTrackingLoop() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            val random = Random()
            val visualizerFreqs = FloatArray(32) { 0.15f }

            while (isActive && isPlayingInternal) {
                val pos = mediaPlayer?.currentPosition?.toLong() ?: 0L
                _currentPositionMs.value = pos

                // Emulate visualizer based on music rhythm
                for (b in 0 until 32) {
                    val base = 0.2f + random.nextFloat() * 0.6f
                    visualizerFreqs[b] = base
                }
                _visualizerFrame.value = VisualizerFrame(visualizerFreqs.clone(), 0.5f)

                delay(100)
            }
        }
    }

    private fun attachNativeEffects(sessionId: Int) {
        if (sessionId == AudioManager.ERROR) return
        try {
            nativeEqualizer?.release()
            nativeBassBoost?.release()
            nativeReverb?.release()
            nativeVirtualizer?.release()

            nativeEqualizer = Equalizer(0, sessionId).apply {
                enabled = true
                applyNativeEqualizer()
            }

            nativeBassBoost = BassBoost(0, sessionId).apply {
                enabled = true
                setStrength((bassBoostAmount * 1000).toInt().toShort())
            }

            nativeVirtualizer = Virtualizer(0, sessionId).apply {
                enabled = true
                setStrength((virtualizerAmount * 1000).toInt().toShort())
            }

            nativeReverb = PresetReverb(0, sessionId).apply {
                preset = mapReverbToNative(reverbRoom)
                enabled = reverbRoom != ReverbRoom.OFF
            }
        } catch (e: Exception) {
            Log.w("SHJAudioEngine", "Native audiofx not available on this device: ${e.message}")
        }
    }

    fun applyEqualizerBands(gainsDb: List<Float>) {
        eqBandGains = gainsDb
        applyNativeEqualizer()
    }

    private fun applyNativeEqualizer() {
        val eq = nativeEqualizer ?: return
        try {
            val numBands = eq.numberOfBands.toInt()
            val minEq = eq.bandLevelRange?.get(0) ?: -1200
            val maxEq = eq.bandLevelRange?.get(1) ?: 1200

            for (i in 0 until numBands) {
                val gainDb = eqBandGains.getOrElse(i) { 0f }
                val millibels = (gainDb * 100).toInt().coerceIn(minEq.toInt(), maxEq.toInt()).toShort()
                eq.setBandLevel(i.toShort(), millibels)
            }
        } catch (e: Exception) {
            Log.w("SHJAudioEngine", "Error setting native EQ: ${e.message}")
        }
    }

    fun applyBassBoost(strength: Float) {
        bassBoostAmount = strength
        try {
            nativeBassBoost?.setStrength((strength.coerceIn(0f, 1f) * 1000).toInt().toShort())
        } catch (e: Exception) {
            Log.w("SHJAudioEngine", "BassBoost set error: ${e.message}")
        }
    }

    fun applyVirtualizer(strength: Float) {
        virtualizerAmount = strength
        try {
            nativeVirtualizer?.setStrength((strength.coerceIn(0f, 1f) * 1000).toInt().toShort())
        } catch (e: Exception) {
            Log.w("SHJAudioEngine", "Virtualizer set error: ${e.message}")
        }
    }

    fun applyReverb(room: ReverbRoom) {
        reverbRoom = room
        try {
            nativeReverb?.apply {
                preset = mapReverbToNative(room)
                enabled = room != ReverbRoom.OFF
            }
        } catch (e: Exception) {
            Log.w("SHJAudioEngine", "PresetReverb set error: ${e.message}")
        }
    }

    private fun mapReverbToNative(room: ReverbRoom): Short {
        return when (room) {
            ReverbRoom.OFF -> PresetReverb.PRESET_NONE
            ReverbRoom.STUDIO -> PresetReverb.PRESET_SMALLROOM
            ReverbRoom.LIVING_ROOM -> PresetReverb.PRESET_MEDIUMROOM
            ReverbRoom.CHAMBER -> PresetReverb.PRESET_LARGEROOM
            ReverbRoom.LARGE_HALL -> PresetReverb.PRESET_MEDIUMHALL
            ReverbRoom.CATHEDRAL -> PresetReverb.PRESET_LARGEHALL
            ReverbRoom.COSMIC_SPACE -> PresetReverb.PRESET_PLATE
        }
    }

    fun applyLoFiSettings(settings: LoFiSettings) {
        lofiSettings = settings
    }

    fun resume() {
        if (!isPlayingInternal && currentTrack != null) {
            val resumePos = _currentPositionMs.value
            currentTrack?.let { playTrack(it, resumePos) }
        }
    }

    fun pause() {
        isPlayingInternal = false
        _isPlaying.value = false
        playbackJob?.cancel()
        try {
            audioTrack?.pause()
            mediaPlayer?.pause()
        } catch (e: Exception) {
            Log.w("SHJAudioEngine", "Pause error: ${e.message}")
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _durationMs.value)
        _currentPositionMs.value = clamped
        if (mediaPlayer != null) {
            try {
                mediaPlayer?.seekTo(clamped.toInt())
            } catch (e: Exception) {
                Log.w("SHJAudioEngine", "Seek error: ${e.message}")
            }
        } else if (isPlayingInternal) {
            currentTrack?.let { playTrack(it, clamped) }
        }
    }

    fun stop() {
        isPlayingInternal = false
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null

        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.w("SHJAudioEngine", "AudioTrack stop error: ${e.message}")
        }

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.w("SHJAudioEngine", "MediaPlayer stop error: ${e.message}")
        }

        try {
            nativeEqualizer?.release()
            nativeBassBoost?.release()
            nativeReverb?.release()
            nativeVirtualizer?.release()
            nativeEqualizer = null
            nativeBassBoost = null
            nativeReverb = null
            nativeVirtualizer = null
        } catch (e: Exception) {
            Log.w("SHJAudioEngine", "Release effects error: ${e.message}")
        }
    }
}
