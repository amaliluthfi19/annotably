package com.amali.annotably.data.network

import OpenLibraryApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API service interface for network calls
 * 
 * Base URL is configured in NetworkModule
 */
interface ApiService {
    
    /**
     * Search for books using OpenLibrary API
     * 
     * @param query Search query (e.g., "harry potter", "tolkien", etc.)
     * @param limit Number of results to return (default: 10)
     * @param offset Offset for pagination (default: 0)
     * @return Response containing OpenLibraryApiResponse with list of books
     * 
     * Example usage:
     * ```
     * val response = apiService.searchBooks("harry potter", limit = 20)
     * ```
     */
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<OpenLibraryApiResponse>
}
