package com.example.arsip.di

import android.content.Context
import com.example.arsip.data.CloudinaryUploader
import com.example.arsip.data.ImageUploader
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
    ): ImageUploader = CloudinaryUploader(ctx, client)
}
