package com.example.arsip.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Book(
    @get:Exclude @set:Exclude var id: String = "",

    val title: String = "",
    val author: String = "",
    val desc: String = "",
    val imageUrls: List<String> = emptyList(),

    val ownerId: String = "",
    val addressText: String = "",
    @get:PropertyName("isAvailable") @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true,

    val lat: Double? = null,
    val lng: Double? = null,
    val createdAt: Timestamp = Timestamp.now()
)
