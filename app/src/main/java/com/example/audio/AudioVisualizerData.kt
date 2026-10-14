package com.example.audio

data class VisualizerFrame(
    val frequencies: FloatArray = FloatArray(32) { 0.1f },
    val peakAmplitude: Float = 0.2f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VisualizerFrame
        return frequencies.contentEquals(other.frequencies) && peakAmplitude == other.peakAmplitude
    }

    override fun hashCode(): Int {
        var result = frequencies.contentHashCode()
        result = 31 * result + peakAmplitude.hashCode()
        return result
    }
}
