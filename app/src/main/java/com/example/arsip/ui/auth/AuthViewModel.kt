package com.example.arsip.ui.auth

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsip.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {
    var isRegister by mutableStateOf(false)
    var email by mutableStateOf("")
    var pass by mutableStateOf("")
    var name by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var addressText by mutableStateOf("")
    var lat by mutableStateOf<Double?>(null)
    var lng by mutableStateOf<Double?>(null)
    var busy by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun onLocationSelected(newLat: Double, newLng: Double, newAddress: String) {
        lat = newLat
        lng = newLng
        addressText = newAddress
    }

    fun submit(onAuthed: () -> Unit) = viewModelScope.launch {
        error = validate()
        if (error != null) return@launch
        busy = true
        runCatching {
            if (isRegister) repo.register(
                email.trim(),
                pass,
                name.trim(),
                phoneNumber.trim(),
                addressText.trim(),
                lat,
                lng
            )
            else repo.login(email.trim(), pass)
        }.onSuccess { onAuthed() }
            .onFailure { e -> error = toHumanMessage(e) }
        busy = false
    }

    fun guest(onAuthed: () -> Unit) = viewModelScope.launch {
        busy = true
        runCatching { repo.guest() }
            .onSuccess { onAuthed() }
            .onFailure { e -> error = toHumanMessage(e) }
        busy = false
    }

    private fun validate(): String? {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Email tidak valid"
        if (pass.length < 6) return "Password minimal 6 karakter"
        if (isRegister && name.isBlank()) return "Nama tidak boleh kosong"
        if (isRegister && phoneNumber.isBlank()) return "Nomor telepon tidak boleh kosong"
        if (isRegister && addressText.isBlank()) return "Alamat tidak boleh kosong"
        return null
    }

    private fun toHumanMessage(e: Throwable): String {
        val msg = e.message ?: return "Terjadi kesalahan"
        return when {
            msg.contains("INVALID_LOGIN_CREDENTIALS", true) -> "Email atau password salah"
            msg.contains("WEAK_PASSWORD", true) -> "Password terlalu lemah (min 6)"
            msg.contains("EMAIL_EXISTS", true) || msg.contains("email address is already in use", true) -> "Email sudah terdaftar"
            msg.contains("TOO_MANY_REQUESTS", true) -> "Terlalu banyak percobaan. Coba lagi nanti."
            else -> msg
        }
    }
}
