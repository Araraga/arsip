package com.example.arsip.data

import android.net.Uri
import com.example.arsip.upload.ImageUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
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

    fun getUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val reg = db.collection("users").document(userId)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val userProfile = snap?.toObject(UserProfile::class.java)
                trySend(userProfile)
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

    suspend fun updatePhoneNumber(phoneNumber: String) {
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid).update("phoneNumber", phoneNumber).await()
    }

    suspend fun updateAddress(address: String) {
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid).update("addressText", address).await()
    }

    suspend fun updateLocation(latitude: Double, longitude: Double) {
        val uid = auth.currentUser!!.uid
        val updates = mapOf(
            "lat" to latitude,
            "lng" to longitude
        )
        db.collection("users").document(uid).update(updates).await()
    }

    suspend fun updateCompleteProfile(
        displayName: String,
        phoneNumber: String,
        address: String,
        latitude: Double,
        longitude: Double
    ) {
        val uid = auth.currentUser!!.uid
        val updates = mapOf(
            "uid" to uid,
            "displayName" to displayName,
            "phoneNumber" to phoneNumber,
            "addressText" to address,
            "lat" to latitude,
            "lng" to longitude,
            "createdAt" to Timestamp.now()
        )
        db.collection("users").document(uid).set(updates, SetOptions.merge()).await()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun logout() {
        auth.signOut()
    }
}