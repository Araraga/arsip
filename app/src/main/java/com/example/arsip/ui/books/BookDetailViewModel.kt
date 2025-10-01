package com.example.arsip.ui.books

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsip.data.Book
import com.example.arsip.data.BooksRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val repo: BooksRepositoryImpl,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val bookId: String = savedStateHandle.get<String>("bookId")!!

    private val _book = MutableStateFlow<Book?>(null)
    val book = _book.asStateFlow()

    var busy by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            // Ambil data buku spesifik dari repository
            repo.getBook(bookId).collect { bookData ->
                _book.value = bookData
            }
        }
    }

    fun toggleAvailability(isAvailable: Boolean) {
        viewModelScope.launch {
            busy = true
            repo.updateAvailability(bookId, isAvailable)
            _book.value = _book.value?.copy(isAvailable = isAvailable) // Update state lokal
            busy = false
        }
    }
    var isDeleting by mutableStateOf(false)
        private set
    fun deleteBook(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isDeleting = true
            busy = true
            repo.deleteBook(bookId)
            onSuccess() // Navigasi kembali setelah sukses
            busy = false
        }
    }
}
