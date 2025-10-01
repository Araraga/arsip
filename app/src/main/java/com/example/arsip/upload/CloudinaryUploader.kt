package com.example.arsip.upload

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class CloudinaryUploader(
    private val contentResolver: ContentResolver,
    private val cloudName: String,
    private val unsignedPreset: String,
    private val folder: String = "books",
    private val client: OkHttpClient = OkHttpClient()
) : ImageUploader {

    override suspend fun uploadMany(uris: List<Uri>): List<String> =
        uris.map { uploadOne(it) }

    override suspend fun uploadOne(uri: Uri): String = withContext(Dispatchers.IO) {
        // Salin stream ke file sementara (OkHttp lebih enak pakai File)
        val temp = File.createTempFile("upl_", ".bin")
        contentResolver.openInputStream(uri)!!.use { input ->
            FileOutputStream(temp).use { out -> input.copyTo(out) }
        }

        val endpoint = "https://api.cloudinary.com/v1_1/$cloudName/upload"
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", unsignedPreset)
            .addFormDataPart("folder", folder)
            .addFormDataPart(
                "file", temp.name,
                temp.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            )
            .build()

        val req = Request.Builder().url(endpoint).post(body).build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("Cloudinary upload failed: ${resp.code}")
            val json = JSONObject(resp.body!!.string())
            json.getString("secure_url") // URL HTTPS publik
        }.also { temp.delete() }
    }
}
