package com.example.arsip.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.util.Date

data class Book(
    @get:Exclude @set:Exclude var id: String = "",

    val title: String = "",
    val author: String = "",
    val desc: String = "",
    val imageUrls: List<String> = emptyList(),

    val ownerId: String = "",           // ID pemilik (relasi ke UserProfile)
    val category: String = "",          // Kategori buku yang dipilih dari dropdown
    val addressText: String = "",       // Alamat lokasi buku

    @get:PropertyName("isAvailable") @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true,

    // ✅ KOORDINAT UNTUK FILTER TERDEKAT:
    val lat: Double? = null,            // Latitude lokasi buku
    val lng: Double? = null,            // Longitude lokasi buku

    // ✅ TIMESTAMP UNTUK FILTER TERBARU:
    val createdAt: Timestamp = Timestamp.now()  // Tanggal upload untuk filter "Terbaru"
) {
    // Helper property untuk mendapatkan Date dari Timestamp
    @get:Exclude
    val timestamp: Date
        get() = createdAt.toDate()
}
