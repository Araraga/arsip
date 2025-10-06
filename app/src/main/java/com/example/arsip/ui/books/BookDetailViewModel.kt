package com.example.arsip.ui.books

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsip.data.Book
import com.example.arsip.data.BooksRepositoryImpl
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
    private val repo: BooksRepositoryImpl,
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val bookId: String = savedStateHandle.get<String>("bookId")!!

    private val _book = MutableStateFlow<Book?>(null)
    val book = _book.asStateFlow()

    // ✅ NEW: Owner profile for WhatsApp functionality
    private val _owner = MutableStateFlow<UserProfile?>(null)
    val owner = _owner.asStateFlow()

    // ✅ NEW: Check if current user is the owner
    var isOwner by mutableStateOf(false)
        private set

    var busy by mutableStateOf(false)
        private set

    var isDeleting by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            // Ambil data buku spesifik dari repository
            repo.getBook(bookId).collect { bookData ->
                _book.value = bookData

                // ✅ NEW: Check if current user is the owner
                bookData?.let { book ->
                    isOwner = repo.getCurrentUserId() == book.ownerId
                    fetchOwnerProfile(book.ownerId)
                }
            }
        }
    }

    // ✅ NEW: Fetch owner profile for WhatsApp functionality
    private fun fetchOwnerProfile(ownerId: String) {
        viewModelScope.launch {
            try {
                // Assuming ProfileRepository has a method to get user by ID
                // You may need to implement this method in ProfileRepository
                profileRepository.getUserProfile(ownerId).collect { userProfile ->
                    _owner.value = userProfile
                }
            } catch (e: Exception) {
                // Handle error - owner info not available
                _owner.value = null
            }
        }
    }

    fun toggleAvailability(isAvailable: Boolean) {
        viewModelScope.launch {
            busy = true
            repo.updateAvailability(bookId, isAvailable)
            _book.value = _book.value?.copy(isAvailable = isAvailable)
            busy = false
        }
    }

    fun deleteBook(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isDeleting = true
            busy = true
            repo.deleteBook(bookId)
            onSuccess()
            busy = false
        }
    }
}
