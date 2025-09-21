package com.example.arsip.data

import android.net.Uri
import com.example.arsip.upload.ImageUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val uploader: ImageUploader
) {
    fun meFlow(): Flow<UserProfile?> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run { trySend(null); close(); return@callbackFlow }
        val reg = db.collection("users").document(uid)
            .addSnapshotListener { snap, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val me = snap?.toObject(UserProfile::class.java)
                trySend(me)
            }
        awaitClose { reg.remove() }
    }

    suspend fun updateName(n: String) {
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid).update("displayName", n).await()
    }

    suspend fun updatePhoto(local: Uri) {
        val url = uploader.uploadOne(local)
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid).update("photoUrl", url).await()
    }
}
