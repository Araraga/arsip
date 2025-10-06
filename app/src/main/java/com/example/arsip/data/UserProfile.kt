package com.example.arsip.data

import java.util.Date

data class UserProfile(
    val uid: String = "",
    val displayName: String = "-",
    val email: String = "",
    val photoUrl: String = "",

    // ✅ FIELD BARU YANG DITAMBAHKAN:
    val phoneNumber: String = "",       // Nomor WhatsApp untuk dihubungi
    val address: String = "",           // Alamat lengkap user
    val latitude: Double = 0.0,         // Koordinat latitude untuk hitung jarak
    val longitude: Double = 0.0,        // Koordinat longitude untuk hitung jarak
    val createdAt: Date = Date()        // Tanggal registrasi
)
