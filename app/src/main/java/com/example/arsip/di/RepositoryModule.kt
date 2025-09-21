package com.example.arsip.di

import com.example.arsip.data.BooksRepository
import com.example.arsip.upload.ImageUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides @Singleton
    fun provideBooksRepository(
        db: FirebaseFirestore,
        auth: FirebaseAuth,
        uploader: ImageUploader
    ): BooksRepository = BooksRepository(db, auth, uploader)
}
