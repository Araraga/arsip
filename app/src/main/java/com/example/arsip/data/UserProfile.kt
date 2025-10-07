package com.example.arsip.data

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class UserProfile(
    val uid: String = "",
    val displayName: String = "-",
    val email: String = "",
    val photoUrl: String = "",
    val phoneNumber: String = "",

    @get:PropertyName("addressText") @set:PropertyName("addressText")
    var address: String = "",

    @get:PropertyName("lat") @set:PropertyName("lat")
    var latitude: Double = 0.0,

    @get:PropertyName("lng") @set:PropertyName("lng")
    var longitude: Double = 0.0,

    val createdAt: Date = Date()
)