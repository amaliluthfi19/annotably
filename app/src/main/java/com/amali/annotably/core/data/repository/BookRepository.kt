package com.amali.annotably.data.repository

import android.util.Log
import com.amali.annotably.core.data.firestore.FirestoreService
import com.amali.annotably.data.model.Book
import com.amali.annotably.data.network.ApiService
import com.amali.annotably.data.network.NetworkResult
import javax.inject.Inject
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query

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
class BookRepository
@Inject
constructor(private val apiService: ApiService, private val firestore: FirestoreService) :
        BaseRepository() {

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
        return safeApiCall { apiService.searchBooks(query, limit, offset) }.let { result ->
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
     * Search for books with pagination support Useful for infinite scroll or load more
     * functionality
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
     * Check if a book already exists in Firestore Compares by book key (unique identifier)
     *
     * @param book Book to check
     * @return true if book exists, false otherwise
     */
    suspend fun bookExists(book: Book): Boolean {
        val key = book.key ?: return false

        return when (val result =
                        firestore.getByQuery("books") { query -> query.whereEqualTo("key", key) }
        ) {
            is NetworkResult.Success -> {
                val documents = result.data ?: emptyList()
                val exists = documents.isNotEmpty()
                Log.d(
                        "BookRepository",
                        "Book with key $key ${if (exists) "exists" else "does not exist"}"
                )
                exists
            }
            is NetworkResult.Error -> {
                Log.e("BookRepository", "Error checking if book exists: ${result.message}")
                // If there's an error checking, assume it doesn't exist to allow the add
                false
            }
            is NetworkResult.Loading -> {
                false
            }
        }
    }

    /**
     * Add a book to Firestore
     *
     * @param book Book to add to Firestore
     * @return NetworkResult indicating success or failure
     */
    suspend fun addBookToFirestore(book: Book): NetworkResult<Unit> {
        Log.d("BookRepository", "Adding book to Firestore: ${book.title}")

        val bookMap =
                hashMapOf(
                        "title" to (book.title ?: ""),
                        "firstPublish" to (book.firstPublish ?: 0),
                        "authorName" to (book.authorName ?: emptyList()),
                        "authorKey" to (book.authorKey ?: emptyList()),
                        "coverId" to (book.coverId ?: 0),
                        "key" to (book.key ?: ""),
                        "createdAt" to (Timestamp.now())
                )

        return when (val result = firestore.create("books", bookMap)) {
            is NetworkResult.Success -> {
                Log.i(
                        "BookRepository",
                        "Successfully added book to Firestore with ID: ${result.data}"
                )
                NetworkResult.Success(Unit)
            }
            is NetworkResult.Error -> {
                Log.e("BookRepository", "Failed to add book to Firestore: ${result.message}")
                NetworkResult.Error(result.message ?: "Failed to add book to Firestore")
            }
            is NetworkResult.Loading -> {
                NetworkResult.Loading()
            }
        }
    }

    /**
     * Get all books from Firestore
     *
     * @return NetworkResult containing list of books from Firestore
     */
    suspend fun getAllBooksFromFirestore(): NetworkResult<List<Book>> {
        Log.d("BookRepository", "Fetching all books from Firestore")

        return when (val result = firestore.getByQuery("books") { query -> query.orderBy("createdAt",
            Query.Direction.DESCENDING).limit(3)}) {
            is NetworkResult.Success -> {
                val documents = result.data ?: emptyList()
                Log.d("BookRepository", "Retrieved ${documents.size} documents from Firestore")

                val books =
                        documents.mapNotNull { document ->
                            try {
                                Book(
                                        title = document["title"] as? String,
                                        firstPublish =
                                                (document["firstPublish"] as? Number)?.toInt(),
                                        authorName =
                                                (document["authorName"] as? List<*>)?.mapNotNull {
                                                    it as? String
                                                },
                                        authorKey =
                                                (document["authorKey"] as? List<*>)?.mapNotNull {
                                                    it as? String
                                                },
                                        coverId = (document["coverId"] as? Number)?.toInt(),
                                        key = document["key"] as? String
                                )
                            } catch (e: Exception) {
                                Log.e(
                                        "BookRepository",
                                        "Error parsing book document ${document["id"]}: ${e.message}",
                                        e
                                )
                                null
                            }
                        }

                Log.i("BookRepository", "Successfully parsed ${books.size} books from Firestore")
                NetworkResult.Success(books)
            }
            is NetworkResult.Error -> {
                Log.e("BookRepository", "Failed to fetch books from Firestore: ${result.message}")
                NetworkResult.Error(result.message ?: "Failed to fetch books from Firestore")
            }
            is NetworkResult.Loading -> {
                NetworkResult.Loading()
            }
        }
    }

    /**
     * Delete a book from Firestore by document ID
     *
     * @param documentId The Firestore document ID
     * @return NetworkResult indicating success or failure
     */
    suspend fun deleteBookById(documentId: String): NetworkResult<Unit> {
        Log.d("BookRepository", "Deleting book with document ID: $documentId from Firestore")

        return when (val result = firestore.delete("books", documentId)) {
            is NetworkResult.Success -> {
                Log.i("BookRepository", "Successfully deleted book with document ID: $documentId")
                NetworkResult.Success(Unit)
            }
            is NetworkResult.Error -> {
                Log.e(
                        "BookRepository",
                        "Failed to delete book with document ID $documentId: ${result.message}"
                )
                NetworkResult.Error(result.message ?: "Failed to delete book from Firestore")
            }
            is NetworkResult.Loading -> {
                NetworkResult.Loading()
            }
        }
    }

    /**
     * Delete a book from Firestore by book key Finds the book by its unique key and deletes it
     *
     * @param bookKey The unique book key (OpenLibrary key)
     * @return NetworkResult indicating success or failure
     */
    suspend fun deleteBookByKey(bookKey: String): NetworkResult<Unit> {
        Log.d("BookRepository", "Deleting book with key: $bookKey from Firestore")

        // First, find the document by key
        return when (val queryResult =
                        firestore.getByQuery("books") { query ->
                            query.whereEqualTo("key", bookKey)
                        }
        ) {
            is NetworkResult.Success -> {
                val documents = queryResult.data ?: emptyList()

                if (documents.isEmpty()) {
                    val errorMessage = "Book with key $bookKey not found in Firestore"
                    Log.w("BookRepository", errorMessage)
                    NetworkResult.Error(errorMessage)
                } else {
                    // Get the document ID from the first matching document
                    val documentId = documents.firstOrNull()?.get("id") as? String
                    if (documentId == null) {
                        val errorMessage = "Document ID not found for book with key $bookKey"
                        Log.e("BookRepository", errorMessage)
                        NetworkResult.Error(errorMessage)
                    } else {
                        // Delete the document by ID
                        deleteBookById(documentId)
                    }
                }
            }
            is NetworkResult.Error -> {
                Log.e(
                        "BookRepository",
                        "Failed to find book with key $bookKey: ${queryResult.message}"
                )
                NetworkResult.Error(queryResult.message ?: "Failed to find book with key $bookKey")
            }
            is NetworkResult.Loading -> {
                NetworkResult.Loading()
            }
        }
    }

    /**
     * Delete a book from Firestore Finds the book by its unique key and deletes it
     *
     * @param book The book to delete
     * @return NetworkResult indicating success or failure
     */
    suspend fun deleteBook(book: Book): NetworkResult<Unit> {
        val key = book.key
        if (key == null) {
            val errorMessage = "Cannot delete book: book key is null"
            Log.e("BookRepository", errorMessage)
            return NetworkResult.Error(errorMessage)
        }

        Log.d("BookRepository", "Deleting book: ${book.title} (key: $key) from Firestore")
        return deleteBookByKey(key)
    }
}
