package com.example.arsip.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsip.data.Book
import com.example.arsip.data.BooksRepository
import com.example.arsip.data.ProfileRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class HomeState(
    val userName: String = "User",
    val userPhotoUrl: String? = null,
    val searchQuery: String = "",
    val selectedFilter: String = "terdekat",
    val radiusKm: Float = 5f,
    val userLocation: LatLng? = null,
    val allBooks: List<Book> = emptyList(),
    val nearbyBooks: List<Book> = emptyList(),
    val highlightedBooks: List<Book> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    fun calculateDistance(book: Book): String {
        if (userLocation == null || book.lat == null || book.lng == null) {
            return book.addressText.split(",").firstOrNull()?.trim() ?: "Lokasi tidak diketahui"
        }

        val distance = calculateDistanceInKm(
            userLocation.latitude,
            userLocation.longitude,
            book.lat,
            book.lng
        )

        return when {
            distance < 1.0 -> "${(distance * 1000).toInt()} m"
            distance < 10.0 -> String.format("%.1f km", distance)
            else -> "${distance.toInt()} km"
        }
    }

    private fun calculateDistanceInKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                loadUserProfile()
                loadBooks()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Init error", e)
                _state.update { it.copy(isLoading = false, userName = "User") }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                profileRepository.meFlow()
                    .catch { e ->
                        Log.e("HomeViewModel", "Error loading profile", e)
                        emit(null)
                    }
                    .collectLatest { userProfile ->
                        // ✅ FIXED: Use real user location from database
                        val userLocation = if (userProfile?.latitude != null && userProfile?.longitude != null &&
                            userProfile.latitude != 0.0 && userProfile.longitude != 0.0) {
                            LatLng(userProfile.latitude, userProfile.longitude)
                        } else {
                            // Default ke Jakarta jika tidak ada lokasi user
                            LatLng(-6.2088, 106.8456)
                        }

                        _state.update {
                            it.copy(
                                userName = userProfile?.displayName?.ifBlank { "User" } ?: "User",
                                userPhotoUrl = userProfile?.photoUrl,
                                userLocation = userLocation
                            )
                        }

                        // ✅ FIXED: Reapply filters immediately when user location changes
                        applyFilters()
                    }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error in loadUserProfile", e)
                _state.update {
                    it.copy(
                        userName = "User",
                        userLocation = LatLng(-6.2088, 106.8456) // Default Jakarta
                    )
                }
                // Apply filters even with default location
                applyFilters()
            }
        }
    }

    fun loadBooks() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, errorMessage = null) }

                booksRepository.allBooksFlow()
                    .catch { e ->
                        Log.e("HomeViewModel", "Error loading books", e)
                        emit(emptyList())
                    }
                    .collectLatest { books ->
                        try {
                            val filtered = filterBooks(books)
                            _state.update { currentState ->
                                currentState.copy(
                                    allBooks = books,
                                    nearbyBooks = filtered,
                                    highlightedBooks = books.filter { it.isAvailable }.take(5),
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("HomeViewModel", "Error filtering books", e)
                            _state.update {
                                it.copy(
                                    allBooks = books,
                                    nearbyBooks = books,
                                    highlightedBooks = books.take(5),
                                    isLoading = false
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error in loadBooks", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onFilterSelect(filter: String) {
        _state.update { it.copy(selectedFilter = filter) }
        applyFilters()
    }

    fun onRadiusChange(radius: Float) {
        _state.update { it.copy(radiusKm = radius) }
        applyFilters()
    }

    fun onLocationPermissionGranted() {
        val defaultLocation = LatLng(-6.2088, 106.8456)
        _state.update { it.copy(userLocation = defaultLocation) }
        applyFilters()
    }

    private fun applyFilters() {
        try {
            val currentState = _state.value
            val filtered = filterBooks(currentState.allBooks)
            _state.update { it.copy(nearbyBooks = filtered) }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error applying filters", e)
        }
    }

    private fun filterBooks(books: List<Book>): List<Book> {
        val currentState = _state.value

        // ✅ FILTER 1: Hanya tampilkan buku yang tersedia (isAvailable = true)
        var filteredBooks = books.filter { it.isAvailable }

        // ✅ FILTER 2: Filter berdasarkan search query
        if (currentState.searchQuery.isNotBlank()) {
            val query = currentState.searchQuery.lowercase()
            filteredBooks = filteredBooks.filter { book ->
                book.title.lowercase().contains(query) ||
                        book.author.lowercase().contains(query) ||
                        book.category.lowercase().contains(query) ||
                        book.desc.lowercase().contains(query)
            }
        }

        // ✅ FILTER 3: Filter berdasarkan lokasi user dan jarak maksimal
        val userLocation = currentState.userLocation
        if (userLocation != null && currentState.selectedFilter == "terdekat") {
            filteredBooks = filteredBooks.filter { book ->
                if (book.lat != null && book.lng != null) {
                    val distance = calculateDistanceInKm(
                        userLocation.latitude,
                        userLocation.longitude,
                        book.lat,
                        book.lng
                    )
                    distance <= 25.0 // Maksimal 25km sesuai dengan filter chip
                } else {
                    false // Hilangkan buku yang tidak memiliki koordinat
                }
            }.sortedBy { book ->
                // Sort berdasarkan jarak terdekat
                if (book.lat != null && book.lng != null) {
                    calculateDistanceInKm(
                        userLocation.latitude,
                        userLocation.longitude,
                        book.lat,
                        book.lng
                    )
                } else {
                    Double.MAX_VALUE
                }
            }
        } else if (currentState.selectedFilter == "terbaru") {
            // ✅ FILTER 4: Sort berdasarkan tanggal terbaru
            filteredBooks = filteredBooks.sortedByDescending { it.createdAt.toDate().time }
        }

        return filteredBooks
    }

    private fun calculateDistanceInKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
