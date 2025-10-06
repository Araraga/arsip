// File: data/BooksRepositoryImpl.kt

package com.example.arsip.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BooksRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val uploader: ImageUploader
) : BooksRepository { // <-- Mengimplementasikan interface

    override fun myBooksFlow(): Flow<List<Book>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        var last = emptyList<Book>()

        val reg = db.collection("books")
            .whereEqualTo("ownerId", uid)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    trySend(last)
                    return@addSnapshotListener
                }
                val items = snap?.documents?.mapNotNull { doc ->
                    val b = doc.toObject(Book::class.java) ?: return@mapNotNull null
                    b.apply { id = doc.id }
                }.orEmpty()
                    .sortedByDescending { it.createdAt.toDate().time }

                last = items
                trySend(items)
            }
        awaitClose { reg.remove() }
    }

    override fun allBooksFlow(): Flow<List<Book>> = callbackFlow {
        var last = emptyList<Book>()

        val reg = db.collection("books")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    trySend(last)
                    return@addSnapshotListener
                }
                val items = snap?.documents?.mapNotNull { doc ->
                    val b = doc.toObject(Book::class.java) ?: return@mapNotNull null
                    b.apply { id = doc.id }
                }.orEmpty()
                    .sortedByDescending { it.createdAt.toDate().time }

                last = items
                trySend(items)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun addBook(
        title: String,
        author: String,
        desc: String,
        images: List<Uri>,
        addressText: String,
        lat: Double?, lng: Double?
    ): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("User belum login")
        val urls = images.map { uploader.uploadOne(it) }
        val entity = Book(
            title = title,
            author = author,
            desc = desc, // <-- Pastikan field 'desc' ada di data class Book
            imageUrls = urls,
            ownerId = uid,
            addressText = addressText,
            lat = lat,
            lng = lng
        )
        db.collection("books").add(entity).await()
    }

    override suspend fun updateBook(
        bookId: String,
        title: String,
        author: String,
        desc: String,
        addressText: String,
        lat: Double?,
        lng: Double?
    ) {
        db.collection("books").document(bookId).update(
            mapOf(
                "title" to title,
                "author" to author,
                "desc" to desc,
                "addressText" to addressText,
                "lat" to lat,
                "lng" to lng
            )
        ).await()
    }

    override suspend fun getMyAddress(): Triple<String, Double?, Double?> {
        val uid = auth.currentUser?.uid ?: return Triple("", null, null)
        val d = db.collection("users").document(uid).get().await()
        val t = d.getString("addressText") ?: ""
        val la = d.getDouble("lat")
        val ln = d.getDouble("lng")
        return Triple(t, la, ln)
    }

    // --- IMPLEMENTASI FUNGSI BARU ---

    override fun getBook(id: String): Flow<Book?> = callbackFlow {
        val reg = db.collection("books").document(id)
            .addSnapshotListener { snap, e ->
                if (e != null || snap == null || !snap.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val book = snap.toObject(Book::class.java)?.apply { this.id = snap.id }
                trySend(book)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun updateAvailability(id: String, isAvailable: Boolean) {
        db.collection("books").document(id).update("isAvailable", isAvailable).await()
    }

    override suspend fun deleteBook(id: String) {
        db.collection("books").document(id).delete().await()
    }
}