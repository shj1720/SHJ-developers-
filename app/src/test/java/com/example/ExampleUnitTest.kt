package com.example

import com.example.model.DefaultSoundPresets
import com.example.data.ExportFormat
import com.example.data.SampleMusicRepository
import com.example.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testTrackDurationFormatting() {
        val track = Track(
            id = "test_1",
            title = "Test Track",
            artist = "Test Artist",
            durationMs = 185000L // 3 minutes 5 seconds
        )
        assertEquals("3:05", track.formattedDuration)
    }

    @Test
    fun testDefaultSoundPresetsIntegrity() {
        val presets = DefaultSoundPresets.presets
        assertTrue(presets.isNotEmpty())
        val flatPreset = presets.first()
        assertEquals("Flat / Pure Reference", flatPreset.name)
        assertEquals(5, flatPreset.bandGainsDb.size)
    }

    @Test
    fun testSampleRepositoryHasBuiltInTracks() {
        val tracks = SampleMusicRepository.builtInTracks
        assertTrue(tracks.size >= 5)
        val defaultPlaylists = SampleMusicRepository.defaultPlaylists
        assertTrue(defaultPlaylists.isNotEmpty())
    }
}
