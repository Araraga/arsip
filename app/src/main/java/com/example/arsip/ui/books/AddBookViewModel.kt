package com.example.arsip.ui.books

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsip.data.BooksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val repo: BooksRepository
) : ViewModel() {
    var title by mutableStateOf("")
    var price by mutableStateOf("")
    var desc  by mutableStateOf("")
    var images by mutableStateOf<List<Uri>>(emptyList()); private set

    var useProfileAddr by mutableStateOf(true)
    var addressText by mutableStateOf("")
    var lat by mutableStateOf<Double?>(null)
    var lng by mutableStateOf<Double?>(null)

    var busy by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)

    init {
        viewModelScope.launch {
            val (t, la, ln) = repo.getMyAddress()
            addressText = t; lat = la; lng = ln
        }
    }

    fun onImagesSelected(uris: List<Uri>) { images = uris }
    fun toggleUseProfileAddr(enable: Boolean) {
        useProfileAddr = enable
        if (enable) viewModelScope.launch {
            val (t, la, ln) = repo.getMyAddress()
            addressText = t; lat = la; lng = ln
        }
    }
    fun setManualAddress(text: String) { if (!useProfileAddr) addressText = text }
    fun setManualLatLng(la: Double, ln: Double) { if (!useProfileAddr) { lat = la; lng = ln } }

    suspend fun addBook(onSuccess: () -> Unit) {
        val p = price.toLongOrNull() ?: 0L
        busy = true
        val r = repo.addBook(title.trim(), p, desc.trim(), images, addressText.trim(), lat, lng)
        busy = false
        r.onSuccess { onSuccess() }
            .onFailure { e -> message = e.localizedMessage ?: "Gagal menyimpan buku" }
    }
}
