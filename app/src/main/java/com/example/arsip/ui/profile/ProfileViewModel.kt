package com.example.arsip.ui.profile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsip.upload.ImageUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val uploader: ImageUploader
) : ViewModel() {

    var name by mutableStateOf("")
    var busy by mutableStateOf(false)

    var tmpAddr by mutableStateOf("")
    var tmpLat by mutableStateOf("")
    var tmpLng by mutableStateOf("")

    private val _snap = MutableStateFlow<DocumentSnapshot?>(null)
    val snap: StateFlow<DocumentSnapshot?> = _snap

    private var reg: ListenerRegistration? = null

    init {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            reg = db.collection("users").document(uid)
                .addSnapshotListener { ds, _ ->
                    _snap.value = ds
                    tmpAddr = ds?.getString("addressText") ?: ""
                    tmpLat  = ds?.getDouble("lat")?.toString() ?: ""
                    tmpLng  = ds?.getDouble("lng")?.toString() ?: ""
                }
        }
    }

    // FUNGSI BARU: Untuk menerima update LatLng dari map picker
    fun onLatLngSelected(lat: Double?, lng: Double?) {
        tmpLat = lat?.toString() ?: ""
        tmpLng = lng?.toString() ?: ""
    }

    fun saveName() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        val currentName = _snap.value?.getString("displayName") ?: ""
        val newName = if (name.isBlank()) currentName else name

        busy = true
        runCatching {
            db.collection("users").document(uid)
                .set(mapOf("displayName" to newName), SetOptions.merge())
                .await()
        }.onSuccess {
            name = "" // reset input
        }.also {
            busy = false
        }
    }

    fun updatePhoto(uri: Uri) = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        busy = true
        runCatching {
            val url = uploader.uploadOne(uri)
            db.collection("users").document(uid)
                .set(mapOf("photoUrl" to url), SetOptions.merge())
                .await()
        }.also {
            busy = false
        }
    }

    fun saveAddress() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        busy = true
        runCatching {
            val data = hashMapOf<String, Any?>(
                "addressText" to tmpAddr,
                "lat" to tmpLat.toDoubleOrNull(),
                "lng" to tmpLng.toDoubleOrNull()
            )
            db.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .await()
        }.also {
            busy = false
        }
    }

    fun logout() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        reg?.remove()
        reg = null
    }
}