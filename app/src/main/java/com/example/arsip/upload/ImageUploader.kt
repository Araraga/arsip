package com.example.arsip.upload

import android.net.Uri

interface ImageUploader {
    suspend fun uploadMany(uris: List<Uri>): List<String>
    suspend fun uploadOne(uri: Uri): String
}
