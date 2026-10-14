package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.Playlist
import com.example.model.Track
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirebaseCloudSyncRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private val testTrack = Track(
        id = "track_neon_01",
        title = "Midnight Drive",
        artist = "SHJ Synth",
        album = "Neon Highways",
        durationMs = 192000L,
        albumArtDrawableName = "art_synthwave_sunset",
        genre = "Synthwave",
        bpm = 105
    )

    private val testPlaylist = Playlist(
        id = "pl_neon_test",
        name = "Synthwave Drift",
        description = "Retro synth grooves",
        tracks = listOf(testTrack)
    )

    @Test
    fun pushAndPullPlaylists_authenticatedUser_succeeds() = runBlocking {
        val userEmail = "shj_user_1@test.com"
        signInTestUser(userEmail)

        val repository = FirebaseCloudSyncRepository(db = firestore, auth = auth)

        val pushResult = withTimeout(10000L) {
            repository.pushPlaylists(listOf(testPlaylist))
        }
        assertTrue("Push should succeed: ${pushResult.exceptionOrNull()?.message}", pushResult.isSuccess)
        assertEquals(1, pushResult.getOrNull())

        val pullResult = withTimeout(10000L) {
            repository.pullPlaylists()
        }
        assertTrue("Pull should succeed: ${pullResult.exceptionOrNull()?.message}", pullResult.isSuccess)
        val pulled = pullResult.getOrNull().orEmpty()
        assertTrue("Should contain neon playlist", pulled.any { it.id == "pl_neon_test" && it.name == "Synthwave Drift" })
        val pulledTrack = pulled.first { it.id == "pl_neon_test" }.tracks.firstOrNull()
        assertNotNull("Track should be parsed", pulledTrack)
        assertEquals("Midnight Drive", pulledTrack?.title)
    }

    @Test
    fun unauthenticatedUser_pushPlaylists_fails() = runBlocking {
        auth.signOut()
        val repository = FirebaseCloudSyncRepository(db = firestore, auth = auth)

        val pushResult = withTimeout(5000L) {
            repository.pushPlaylists(listOf(testPlaylist))
        }
        assertFalse("Unauthenticated push should fail", pushResult.isSuccess)
    }

    @Test
    fun crossUserAccess_cannotReadAnotherUsersData() = runBlocking {
        val userAlice = "alice_music@test.com"
        val userBob = "bob_music@test.com"

        val aliceUid = signInTestUser(userAlice)
        val aliceRepo = FirebaseCloudSyncRepository(db = firestore, auth = auth)
        val pushResult = withTimeout(10000L) {
            aliceRepo.pushPlaylists(listOf(testPlaylist))
        }
        assertTrue("Alice push must succeed", pushResult.isSuccess)

        // Switch to Bob
        val bobUid = signInTestUser(userBob)
        assertTrue(aliceUid != bobUid)

        // Attempt to read Alice's playlist directly from Bob's auth context
        try {
            firestore.collection("users").document(aliceUid).collection("playlists").get().await()
            fail("Bob should NOT be allowed to read Alice's playlists")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }
}
