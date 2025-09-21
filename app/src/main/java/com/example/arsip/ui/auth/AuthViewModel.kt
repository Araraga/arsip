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
    var busy by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun submit(onOk: () -> Unit) = viewModelScope.launch {
        busy = true; error = null
        runCatching {
            if (isRegister) repo.register(email.trim(), pass, name.trim())
            else repo.login(email.trim(), pass)
        }.onSuccess { onOk() }.onFailure { error = it.localizedMessage }
        busy = false
    }

    fun guest(onOk: () -> Unit) = viewModelScope.launch {
        busy = true
        runCatching { repo.guest() }.onSuccess { onOk() }.onFailure { error = it.message }
        busy = false
    }
}
