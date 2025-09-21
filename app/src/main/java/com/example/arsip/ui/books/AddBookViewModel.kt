package com.example.arsip.ui.books

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsip.data.BooksRepository   // ⬅️ pastikan import ini
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
    var images by mutableStateOf<List<Uri>>(emptyList())
        private set
    var busy by mutableStateOf(false)

    fun onImagesSelected(uris: List<Uri>) { images = uris }

    fun addBook() {
        val p = price.toLongOrNull() ?: 0L
        viewModelScope.launch {
            busy = true
            runCatching { repo.addBook(title.trim(), p, desc.trim(), images) }
            busy = false
        }
    }
}
