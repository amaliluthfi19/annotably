package com.amali.annotably.data.repository


import android.util.Log
import androidx.compose.ui.res.stringResource
import com.amali.annotably.R
import com.amali.annotably.data.model.Book
import com.amali.annotably.data.network.ApiService
import com.amali.annotably.data.network.NetworkResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
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
    private val apiService: ApiService,
    private val firestore: FirebaseFirestore
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
    
    /**
     * Check if a book already exists in Firestore
     * Compares by title and primary author (case-insensitive)
     * 
     * @param book Book to check
     * @return true if book exists, false otherwise
     */
    suspend fun bookExists(book: Book): Boolean {
        return try {
            val key = book.key ?: return false
            
            // Query by title (case-sensitive in Firestore, but we'll compare case-insensitively)
            val querySnapshot = firestore.collection("books")
                .whereEqualTo("key", key)
                .get()
                .await()
            
            // Check if any document has matching title (case-insensitive) and author (case-insensitive)
            querySnapshot.documents.any { document ->
                val docKey = (document.get("key") as? String)
                Log.d("Book Key", "this is book key $key and $docKey and ${key.equals(docKey)}")
                docKey.equals(key)
            }
        } catch (e: Exception) {
            // If there's an error checking, assume it doesn't exist to allow the add
            false
        }
    }
    
    /**
     * Add a book to Firestore
     * 
     * @param book Book to add to Firestore
     * @return NetworkResult indicating success or failure
     */
    suspend fun addBookToFirestore(book: Book): NetworkResult<Unit> {
        return try {
            val bookMap = hashMapOf(
                "title" to (book.title ?: ""),
                "firstPublish" to (book.firstPublish ?: 0),
                "authorName" to (book.authorName ?: emptyList()),
                "authorKey" to (book.authorKey ?: emptyList()),
                "coverId" to (book.coverId ?: 0),
                "key" to (book.key ?: "")
            )
            
            firestore.collection("books")
                .add(bookMap)
                .await()
            
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to add book to Firestore")
        }
    }
}
