package com.example.arsip.di

import android.app.Application
import com.example.arsip.BuildConfig
import com.example.arsip.upload.CloudinaryUploader
import com.example.arsip.upload.ImageUploader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UploadModule {
    @Provides @Singleton
    fun provideImageUploader(app: Application): ImageUploader =
        CloudinaryUploader(
            contentResolver = app.contentResolver,
            cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME,
            unsignedPreset = BuildConfig.CLOUDINARY_PRESET,
            folder = "books"
        )
}
