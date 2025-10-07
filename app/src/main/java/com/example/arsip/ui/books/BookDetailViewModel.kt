package com.example.arsip.ui.books

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsip.data.Book
import com.example.arsip.data.BooksRepository // <-- FIX 1: Gunakan Interface, bukan Implementasi
import com.example.arsip.data.ProfileRepository
import com.example.arsip.data.UserProfile
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
    private val repo: BooksRepository, // <-- FIX 1: Gunakan Interface, bukan Implementasi
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val bookId: String = savedStateHandle.get<String>("bookId")!!

    private val _book = MutableStateFlow<Book?>(null)
    val book = _book.asStateFlow()

    private val _owner = MutableStateFlow<UserProfile?>(null)
    val owner = _owner.asStateFlow()

    var isOwner by mutableStateOf(false)
        private set

    var busy by mutableStateOf(false)
        private set

    var isDeleting by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            repo.getBook(bookId).collect { bookData ->
                _book.value = bookData
                bookData?.let { book ->
                    isOwner = repo.getCurrentUserId() == book.ownerId
                    fetchOwnerProfile(book.ownerId)
                }
            }
        }
    }

    private fun fetchOwnerProfile(ownerId: String) {
        viewModelScope.launch {
            try {
                profileRepository.getUserProfile(ownerId).collect { userProfile ->
                    _owner.value = userProfile
                }
            } catch (e: Exception) {
                _owner.value = null
            }
        }
    }

    fun toggleAvailability(isAvailable: Boolean) {
        viewModelScope.launch {
            busy = true
            repo.updateAvailability(bookId, isAvailable)
            busy = false
        }
    }

    fun deleteBook(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isDeleting = true
            repo.deleteBook(bookId)
            onSuccess()
        }
    }
}