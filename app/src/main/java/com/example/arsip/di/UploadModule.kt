package com.example.arsip.di

import android.content.Context
import com.example.arsip.BuildConfig
import com.example.arsip.upload.CloudinaryUploader
import com.example.arsip.upload.ImageUploader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UploaderModule {

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient()

    @Provides @Singleton
    fun provideImageUploader(
        @ApplicationContext ctx: Context,
        client: OkHttpClient
    ): ImageUploader = CloudinaryUploader(
        contentResolver = ctx.contentResolver,
        cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME,
        unsignedPreset = BuildConfig.CLOUDINARY_PRESET,
        folder = "books",
        client = client
    )
}
