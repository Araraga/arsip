package com.example.arsip.data

import android.net.Uri

interface ImageUploader {
    /** Mengunggah satu gambar dan mengembalikan URL publik (https). */
    suspend fun uploadOne(uri: Uri): String
}
