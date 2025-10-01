// File: di/RepositoryBindsModule.kt

package com.example.arsip.di

import com.example.arsip.data.BooksRepository
import com.example.arsip.data.BooksRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindsModule {

    @Binds
    abstract fun bindBooksRepository(
        impl: BooksRepositoryImpl
    ): BooksRepository
}