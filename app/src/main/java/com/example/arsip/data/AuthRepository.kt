package com.example.arsip.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun register(email: String, pass: String, name: String) {
        auth.createUserWithEmailAndPassword(email, pass).await()
        val uid = auth.currentUser?.uid ?: error("User tidak ditemukan setelah register")

        val data = mapOf(
            "displayName" to name,
            "photoUrl" to "",
            "addressText" to "",
            "lat" to null,
            "lng" to null,
            "createdAt" to Timestamp.now()
        )
        db.collection("users").document(uid).set(data).await()
    }

    suspend fun login(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass).await()
    }

    suspend fun guest() {
        if (auth.currentUser == null) auth.signInAnonymously().await()
    }

    fun logout() = auth.signOut()
}
