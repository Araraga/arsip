package com.example.arsip.ui.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsip.data.Book
import com.example.arsip.data.BooksRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyBooksViewModel @Inject constructor(
    private val repo: BooksRepositoryImpl
) : ViewModel() {

    private val _items = MutableStateFlow<List<Book>>(emptyList())
    val items: StateFlow<List<Book>> = _items

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init {
        viewModelScope.launch {
            repo.myBooksFlow().collect { list ->
                _items.value = list
                _loading.value = false
            }
        }
    }
}
