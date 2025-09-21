package com.example.arsip.data

import android.net.Uri
import com.example.arsip.upload.ImageUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val uploader: ImageUploader
) {
    /** Tambah buku baru: upload gambar ke pihak ketiga lalu simpan URL ke Firestore */
    suspend fun addBook(
        title: String,
        price: Long,
        desc: String,
        images: List<Uri>
    ) {
        val uid = auth.currentUser?.uid ?: error("User belum login")
        val urls: List<String> = images.map { uploader.uploadOne(it) }   // upload & ambil URL

        val doc = Book(
            title = title,
            price = price,
            description = desc,
            imageUrls = urls,
            ownerId = uid
        )
        db.collection("books").add(doc).await()
    }

    /** (opsional) flow daftar buku milik user untuk layar “Buku Saya” */
    fun myBooksFlow(): Flow<List<Book>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val reg = db.collection("books")
            .whereEqualTo("ownerId", uid)
            .addSnapshotListener { snaps, e ->
                if (e != null) return@addSnapshotListener
                val list = snaps?.documents?.map { d ->
                    d.toObject(Book::class.java)?.copy(id = d.id)
                }?.filterNotNull().orEmpty()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }
}
