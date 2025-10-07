package com.example.arsip.ui.books

import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsip.data.Book
import com.example.arsip.data.BooksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditBookViewModel @Inject constructor(
    private val repo: BooksRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val bookId: String = savedStateHandle.get<String>("bookId")!!

    var book by mutableStateOf<Book?>(null)
        private set

    var title by mutableStateOf("")
    var author by mutableStateOf("")
    var desc by mutableStateOf("")
    var selectedCategory by mutableStateOf("")
    var addressText by mutableStateOf("")
    var lat by mutableStateOf<Double?>(null)
    var lng by mutableStateOf<Double?>(null)

    var busy by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)

    init {
        viewModelScope.launch {
            book = repo.getBook(bookId).first()
            book?.let {
                title = it.title
                author = it.author
                desc = it.desc
                selectedCategory = it.category
                addressText = it.addressText
                lat = it.lat
                lng = it.lng
            }
        }
    }

    /**
     * Fungsi untuk menerima update lokasi dari Map Picker.
     * Dipanggil dari AppNav.kt.
     */
    fun onAddressUpdate(newAddress: String, newLat: Double?, newLng: Double?) {
        addressText = newAddress
        lat = newLat
        lng = newLng
    }

    /**
     * Menyimpan semua perubahan ke repository.
     */
    suspend fun saveBook(onSuccess: () -> Unit) {
        if (title.isBlank() || author.isBlank()) {
            message = "Judul dan Penulis tidak boleh kosong."
            return
        }

        busy = true
        repo.updateBook(
            bookId = bookId,
            title = title.trim(),
            author = author.trim(),
            desc = desc.trim(),
            category = selectedCategory.trim(),
            addressText = addressText.trim(),
            lat = lat,
            lng = lng
        )
        busy = false
        onSuccess()
    }
}