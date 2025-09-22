package com.example.arsip.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val uploader: ImageUploader
) {
    fun myBooksFlow(): Flow<List<Book>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        var last = emptyList<Book>()

        val reg = db.collection("books")
            .whereEqualTo("ownerId", uid)       // ⬅ no orderBy; hindari index wajib
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    // JANGAN clear list; biarkan tampil data terakhir
                    trySend(last)
                    return@addSnapshotListener
                }
                val items = snap?.documents?.mapNotNull { doc ->
                    val b = doc.toObject(Book::class.java) ?: return@mapNotNull null
                    b.apply { id = doc.id }
                }.orEmpty()
                    .sortedByDescending { it.createdAt.toDate().time } // sort di klien

                last = items
                trySend(items)
            }
        awaitClose { reg.remove() }
    }

    suspend fun addBook(
        title: String,
        price: Long,
        desc: String,
        images: List<Uri>,
        addressText: String,
        lat: Double?, lng: Double?
    ): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("User belum login")
        val urls = images.map { uploader.uploadOne(it) }
        val entity = Book(
            title = title,
            price = price,
            description = desc,
            imageUrls = urls,
            ownerId = uid,
            addressText = addressText,
            lat = lat,
            lng = lng
        )
        db.collection("books").add(entity).await()
    }

    suspend fun getMyAddress(): Triple<String, Double?, Double?> {
        val uid = auth.currentUser?.uid ?: return Triple("", null, null)
        val d = db.collection("users").document(uid).get().await()
        val t = d.getString("addressText") ?: ""
        val la = d.getDouble("lat")
        val ln = d.getDouble("lng")
        return Triple(t, la, ln)
    }
}
