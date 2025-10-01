package com.example.arsip.data

import android.content.Context
import android.net.Uri
import com.example.arsip.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class CloudinaryUploader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: OkHttpClient
) : ImageUploader {

    override suspend fun uploadOne(uri: Uri): String = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Tidak bisa membuka file gambar")

        val form = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", BuildConfig.CLOUDINARY_PRESET)
            .addFormDataPart(
                "file",
                "image.jpg",
                bytes.toRequestBody("image/*".toMediaType())
            )
            .build()

        val url = "https://api.cloudinary.com/v1_1/${BuildConfig.CLOUDINARY_CLOUD_NAME}/image/upload"
        val req = Request.Builder().url(url).post(form).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("Upload gagal: ${resp.code}")
            val body = resp.body?.string() ?: error("Respon kosong")
            JSONObject(body).getString("secure_url") // URL https
        }
    }
}
