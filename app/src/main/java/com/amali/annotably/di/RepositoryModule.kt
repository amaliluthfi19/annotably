package com.amali.annotably.di

import com.amali.annotably.data.network.ApiService
import com.amali.annotably.data.repository.BookRepository
import com.amali.annotably.data.repository.ExampleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideExampleRepository(apiService: ApiService): ExampleRepository {
        return ExampleRepository(apiService)
    }
    
    @Provides
    @Singleton
    fun provideBookRepository(apiService: ApiService): BookRepository {
        return BookRepository(apiService)
    }
}
