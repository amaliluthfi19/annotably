package com.amali.annotably.data.repository


import com.amali.annotably.data.model.Book
import com.amali.annotably.data.network.ApiService
import com.amali.annotably.data.network.NetworkResult
import javax.inject.Inject

/**
 * Repository for book-related operations using OpenLibrary API
 * 
 * This repository handles:
 * - Searching for books by query
 * - Fetching book details
 * 
 * Usage in ViewModel:
 * ```
 * @HiltViewModel
 * class SearchViewModel @Inject constructor(
 *     private val bookRepository: BookRepository
 * ) : ViewModel() {
 *     
 *     fun searchBooks(query: String) {
 *         viewModelScope.launch {
 *             when (val result = bookRepository.searchBooks(query)) {
 *                 is NetworkResult.Success -> {
 *                     val books = result.data
 *                     // Handle books
 *                 }
 *                 is NetworkResult.Error -> {
 *                     // Handle error
 *                 }
 *                 is NetworkResult.Loading -> {
 *                     // Show loading
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 */
class BookRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {
    
    /**
     * Search for books by query
     * 
     * @param query Search query (e.g., "harry potter", "tolkien")
     * @param limit Number of results to return (default: 10)
     * @param offset Offset for pagination (default: 0)
     * @return NetworkResult containing list of books
     */
    suspend fun searchBooks(
        query: String,
        limit: Int = 10,
        offset: Int = 0
    ): NetworkResult<List<Book>> {
        return safeApiCall {
            apiService.searchBooks(query, limit, offset)
        }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    NetworkResult.Success(result.data?.docs ?: emptyList())
                }
                is NetworkResult.Error -> {
                    NetworkResult.Error(result.message ?: "Unknown error")
                }
                is NetworkResult.Loading -> {
                    NetworkResult.Loading()
                }
            }
        }
    }
    
    /**
     * Search for books with pagination support
     * Useful for infinite scroll or load more functionality
     * 
     * @param query Search query
     * @param page Page number (starts from 0)
     * @param pageSize Number of items per page
     * @return NetworkResult containing list of books for the requested page
     */
    suspend fun searchBooksWithPagination(
        query: String,
        page: Int = 0,
        pageSize: Int = 10
    ): NetworkResult<List<Book>> {
        val offset = page * pageSize
        return searchBooks(query, limit = pageSize, offset = offset)
    }
}
