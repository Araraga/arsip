// File: data/BooksRepository.kt

package com.example.arsip.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface BooksRepository {

    fun myBooksFlow(): Flow<List<Book>>

    suspend fun addBook(
        title: String,
        author: String,
        desc: String,
        images: List<Uri>,
        addressText: String,
        lat: Double?,
        lng: Double?
    ): Result<Unit>

    suspend fun getMyAddress(): Triple<String, Double?, Double?>

    // Fungsi-fungsi untuk halaman detail
    fun getBook(id: String): Flow<Book?>
    suspend fun updateAvailability(id: String, isAvailable: Boolean)
    suspend fun updateBook(
        bookId: String,
        title: String,
        author: String,
        desc: String,
        addressText: String,
        lat: Double?,
        lng: Double?
    )
    suspend fun deleteBook(id: String)

}