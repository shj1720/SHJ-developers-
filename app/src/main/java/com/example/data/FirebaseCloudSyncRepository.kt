package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.Playlist
import com.example.model.SoundProfilePreset
import com.example.model.Track
import com.example.model.VintageLoFiTone
import com.example.model.ReverbRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "FirebaseCloudSync"

class FirebaseCloudSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    // Secondary constructor resolving custom database ID safely
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = FirebaseAuth.getInstance()
    )

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun signInWithGoogleCredential(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase user is null after sign in")
            syncUserProfile(user)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase sign-in failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    private suspend fun syncUserProfile(user: FirebaseUser) {
        val userDoc = db.collection("users").document(user.uid)
        val data = hashMapOf<String, Any>(
            "email" to (user.email ?: "unknown@shj.music"),
            "displayName" to (user.displayName ?: "SHJ Listener"),
            "photoUrl" to (user.photoUrl?.toString() ?: ""),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        userDoc.set(data, SetOptions.merge()).await()
    }

    /**
     * Uploads playlists to Firestore under the authenticated user's scope
     */
    suspend fun pushPlaylists(playlists: List<Playlist>): Result<Int> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User is not signed in to sync cloud playlists"))

        return try {
            val batch = db.batch()
            val userPlaylistsCol = db.collection("users").document(uid).collection("playlists")

            playlists.forEach { playlist ->
                val docRef = userPlaylistsCol.document(playlist.id)

                val tracksArray = JSONArray()
                playlist.tracks.forEach { tr ->
                    val trObj = JSONObject().apply {
                        put("id", tr.id)
                        put("title", tr.title)
                        put("artist", tr.artist)
                        put("album", tr.album)
                        put("durationMs", tr.durationMs)
                        put("albumArtDrawableName", tr.albumArtDrawableName ?: "art_lofi_study")
                        put("genre", tr.genre)
                        put("isFavorite", tr.isFavorite)
                        put("bpm", tr.bpm)
                    }
                    tracksArray.put(trObj)
                }

                val playlistData = hashMapOf<String, Any>(
                    "id" to playlist.id,
                    "name" to playlist.name,
                    "description" to playlist.description,
                    "tracksCount" to playlist.tracks.size,
                    "totalDurationMs" to playlist.totalDurationMs.toInt(),
                    "tracksJson" to tracksArray.toString(),
                    "userId" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(docRef, playlistData, SetOptions.merge())
            }

            batch.commit().await()
            Result.success(playlists.size)
        } catch (e: Exception) {
            Log.e(TAG, "Failed pushing playlists to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Pulls playlists from Firestore for the current user
     */
    suspend fun pullPlaylists(): Result<List<Playlist>> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User is not signed in to pull playlists"))

        return try {
            val querySnapshot = db.collection("users").document(uid).collection("playlists").get().await()
            val pulledPlaylists = querySnapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: return@mapNotNull null
                val desc = doc.getString("description") ?: ""
                val tracksJson = doc.getString("tracksJson") ?: "[]"

                val parsedTracks = mutableListOf<Track>()
                try {
                    val arr = JSONArray(tracksJson)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        parsedTracks.add(
                            Track(
                                id = obj.optString("id", "remote_tr_$i"),
                                title = obj.optString("title", "Unknown Track"),
                                artist = obj.optString("artist", "Unknown Artist"),
                                album = obj.optString("album", "Cloud Synced"),
                                durationMs = obj.optLong("durationMs", 180000L),
                                albumArtDrawableName = obj.optString("albumArtDrawableName", "art_lofi_study"),
                                genre = obj.optString("genre", "Lo-Fi / Chill"),
                                isFavorite = obj.optBoolean("isFavorite", false),
                                isDownloaded = false,
                                bpm = obj.optInt("bpm", 80)
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing tracksJson: ${e.message}")
                }

                Playlist(
                    id = id,
                    name = name,
                    description = desc,
                    coverDrawableName = "art_lofi_study",
                    tracks = parsedTracks,
                    isSyncedToCloud = true
                )
            }
            Result.success(pulledPlaylists)
        } catch (e: Exception) {
            Log.e(TAG, "Failed pulling playlists from Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Observes real-time playlist updates from Firestore
     */
    fun observePlaylists(userId: String): Flow<List<Playlist>> = callbackFlow {
        val colRef = db.collection("users").document(userId).collection("playlists")
        val listener: ListenerRegistration = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Snapshot error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val desc = doc.getString("description") ?: ""
                    Playlist(
                        id = id,
                        name = name,
                        description = desc,
                        coverDrawableName = "art_lofi_study",
                        isSyncedToCloud = true
                    )
                }
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    /**
     * Saves a custom equalizer preset to Firestore
     */
    suspend fun pushCustomPreset(preset: SoundProfilePreset): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User is not signed in"))
        return try {
            val docRef = db.collection("users").document(uid).collection("presets").document(preset.id)
            val data = hashMapOf<String, Any>(
                "id" to preset.id,
                "name" to preset.name,
                "bandGains" to preset.bandGainsDb,
                "bassBoost" to preset.bassBoost,
                "virtualizer3D" to preset.virtualizer3D,
                "reverbRoom" to preset.reverbRoom.name,
                "loFiPreset" to preset.loFiPreset.name,
                "userId" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed pushing preset", e)
            Result.failure(e)
        }
    }
}
