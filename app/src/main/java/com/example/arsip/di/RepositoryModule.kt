package com.example.arsip.di

import com.example.arsip.data.AuthRepository
import com.example.arsip.data.BooksRepository
import com.example.arsip.data.ImageUploader
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
    fun provideAuthRepository(
        auth: FirebaseAuth,
        db: FirebaseFirestore
    ): AuthRepository = AuthRepository(auth, db)

    @Provides @Singleton
    fun provideBooksRepository(
        auth: FirebaseAuth,
        db: FirebaseFirestore,
        uploader: ImageUploader
    ): BooksRepository = BooksRepository(auth, db, uploader)
}
