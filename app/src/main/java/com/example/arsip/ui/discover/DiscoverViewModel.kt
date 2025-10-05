package com.example.arsip.ui.discover

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsip.data.Book
import com.example.arsip.data.BooksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DiscoverSort { NEWEST, NEAREST }

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repo: BooksRepository
) : ViewModel() {
    val allBooks: StateFlow<List<Book>> = repo.allBooksFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val query = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("")
    val sort = MutableStateFlow(DiscoverSort.NEWEST)

    // location for distance sorting (e.g., user's stored address)
    var userLat by mutableStateOf<Double?>(null)
    var userLng by mutableStateOf<Double?>(null)

    val categories: StateFlow<List<String>> = allBooks.map { list ->
        list.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val filtered: StateFlow<List<Book>> = combine(allBooks, query, selectedCategory, sort) { books, q, cat, s ->
        var res = books.asSequence()
            .filter { it.isAvailable }
            .filter {
                val qq = q.trim().lowercase()
                if (qq.isBlank()) true else
                    it.title.lowercase().contains(qq) || it.category.lowercase().contains(qq)
            }
            .filter {
                val c = cat.trim()
                if (c.isBlank()) true else it.category.equals(c, ignoreCase = true)
            }

        res = when (s) {
            DiscoverSort.NEWEST -> res.sortedByDescending { it.createdAt.toDate().time }
            DiscoverSort.NEAREST -> res.sortedWith(compareBy { bookDistanceMeters(it) ?: Double.MAX_VALUE })
        }

        res.toList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun bookDistanceMeters(b: Book): Double? {
        val la = b.lat ?: return null
        val ln = b.lng ?: return null
        val ula = userLat ?: return null
        val uln = userLng ?: return null
        return haversineMeters(ula, uln, la, ln)
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val dp = Math.toRadians(lat2 - lat1)
        val dl = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dp / 2) * kotlin.math.sin(dp / 2) +
            kotlin.math.cos(p1) * kotlin.math.cos(p2) *
            kotlin.math.sin(dl / 2) * kotlin.math.sin(dl / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return r * c
    }

    init {
        // Ambil lokasi user dari profil jika ada untuk sorting NEAREST dan pusat peta
        viewModelScope.launch {
            runCatching { repo.getMyAddress() }
                .onSuccess { (_, la, ln) ->
                    userLat = la
                    userLng = ln
                }
        }
    }
}


