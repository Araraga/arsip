package com.example.arsip.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.Timestamp

data class Book(
    @get:Exclude @set:Exclude var id: String = "",

    val title: String = "",
    val price: Long = 0L,
    val description: String = "",
    val imageUrls: List<String> = emptyList(),

    val ownerId: String = "",
    val addressText: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val createdAt: Timestamp = Timestamp.now()
)
