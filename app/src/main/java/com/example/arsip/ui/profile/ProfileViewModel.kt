package com.example.arsip.ui.profile

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsip.data.ProfileRepository
import com.example.arsip.data.UserProfile
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    val me = repo.meFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var name by mutableStateOf("")
    var busy by mutableStateOf(false)

    fun saveName() = viewModelScope.launch {
        val n = name.trim()
        if (n.isNotEmpty()) repo.updateName(n)
    }

    fun updatePhoto(uri: Uri) = viewModelScope.launch {
        busy = true
        runCatching { repo.updatePhoto(uri) }
        busy = false
    }

    fun logout() = auth.signOut()
}
