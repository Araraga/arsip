package com.example.arsip.data

import com.google.firebase.Timestamp

data class Book(
    val id: String? = null,              // id dokumen (untuk UI; tidak disimpan otomatis)
    val title: String = "",
    val price: Long = 0,
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val ownerId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)
