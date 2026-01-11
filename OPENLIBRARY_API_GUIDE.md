# OpenLibrary API Integration

## Overview

This project integrates with the [OpenLibrary Search API](https://openlibrary.org/dev/docs/api/search) to search for books and retrieve book information.

## Data Models

### OpenLibraryApiResponse

The main response model from the OpenLibrary API.

```kotlin
data class OpenLibraryApiResponse(
    val numFound: Int,              // Total number of results found
    val start: Int,                 // Starting index of results
    val numFoundExact: Boolean,     // Whether the count is exact
    val query: String,              // The search query
    val offset: Int?,               // Offset for pagination
    val docs: List<Book>            // List of books
)
```

### Book

Contains essential book information with helper methods for image URLs.

```kotlin
data class Book(
    val title: String?,             // Book title
    val firstPublish: Int?,         // First publication year
    val authorName: List<String>?,  // List of author names
    val authorKey: List<String>?,   // OpenLibrary author IDs
    val coverId: Int?               // Cover image ID
)
```

## Image URLs

### Cover Images

Format: `https://covers.openlibrary.org/b/id/{cover_i}-{size}.jpg`

**Helper method:**
```kotlin
val book: Book = ...
val coverUrl = book.getCoverImageUrl("L")  // Returns cover URL
```

**Size options:**
- `"S"` - Small (max dimensions)
- `"M"` - Medium (default)
- `"L"` - Large (highest quality)

**Example:**
```
https://covers.openlibrary.org/b/id/10521270-L.jpg
```

### Author Images

Format: `https://covers.openlibrary.org/a/olid/{author_key}-{size}.jpg`

**Helper method:**
```kotlin
val book: Book = ...
val authorImageUrl = book.getAuthorImageUrl("M")  // Returns author image URL
```

**Size options:**
- `"S"` - Small
- `"M"` - Medium (default)
- `"L"` - Large

**Example:**
```
https://covers.openlibrary.org/a/olid/OL23919A-M.jpg
```

## API Endpoints

### Search Books

**Endpoint:** `GET /search.json`

**Parameters:**
- `q` (required) - Search query
- `limit` (optional) - Number of results (default: 10)
- `offset` (optional) - Offset for pagination (default: 0)

**Example:**
```kotlin
interface ApiService {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<OpenLibraryApiResponse>
}
```

## Repository Usage

### BookRepository

The `BookRepository` provides methods for searching books with proper error handling.

```kotlin
class BookRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {
    
    suspend fun searchBooks(
        query: String,
        limit: Int = 10,
        offset: Int = 0
    ): NetworkResult<List<Book>>
    
    suspend fun searchBooksWithPagination(
        query: String,
        page: Int = 0,
        pageSize: Int = 10
    ): NetworkResult<List<Book>>
}
```

## ViewModel Example

Here's how to use the repository in a ViewModel:

```kotlin
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    
    private val _searchResults = MutableStateFlow<NetworkResult<List<Book>>>(NetworkResult.Loading())
    val searchResults: StateFlow<NetworkResult<List<Book>>> = _searchResults
    
    fun searchBooks(query: String) {
        viewModelScope.launch {
            _searchResults.value = NetworkResult.Loading()
            _searchResults.value = bookRepository.searchBooks(query, limit = 20)
        }
    }
    
    // For pagination
    fun loadMoreBooks(query: String, page: Int) {
        viewModelScope.launch {
            val result = bookRepository.searchBooksWithPagination(query, page)
            // Handle pagination result
        }
    }
}
```

## Composable Example

Using the ViewModel in a Composable:

```kotlin
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchResults by viewModel.searchResults.collectAsState()
    
    Column {
        SearchBar(
            onSearch = { query -> viewModel.searchBooks(query) }
        )
        
        when (val result = searchResults) {
            is NetworkResult.Loading -> {
                CircularProgressIndicator()
            }
            is NetworkResult.Success -> {
                LazyColumn {
                    items(result.data ?: emptyList()) { book ->
                        BookItem(book = book)
                    }
                }
            }
            is NetworkResult.Error -> {
                Text("Error: ${result.message}")
            }
        }
    }
}

@Composable
fun BookItem(book: Book) {
    Row(modifier = Modifier.padding(16.dp)) {
        // Cover image
        AsyncImage(
            model = book.getCoverImageUrl("M"),
            contentDescription = book.title,
            modifier = Modifier.size(80.dp, 120.dp)
        )
        
        Column(modifier = Modifier.padding(start = 16.dp)) {
            // Title
            Text(
                text = book.title ?: "Unknown Title",
                style = MaterialTheme.typography.titleMedium
            )
            
            // Author
            Text(
                text = book.getFormattedAuthors(),
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Publication year
            book.firstPublish?.let { year ->
                Text(
                    text = "First published: $year",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
```

## Helper Methods in Book Model

### `getAuthorImageUrl(size: String = "M"): String?`
Returns the author image URL or null if author key is not available.

```kotlin
val authorImage = book.getAuthorImageUrl("L")
```

### `getCoverImageUrl(size: String = "L"): String?`
Returns the cover image URL or null if cover ID is not available.

```kotlin
val coverImage = book.getCoverImageUrl("L")
```

### `getPrimaryAuthor(): String?`
Returns the first author name or null.

```kotlin
val mainAuthor = book.getPrimaryAuthor()
```

### `getFormattedAuthors(): String`
Returns comma-separated author names or "Unknown Author".

```kotlin
val authors = book.getFormattedAuthors()  // "J. K. Rowling, John Doe"
```

## Error Handling

The repository returns `NetworkResult` which has three states:

```kotlin
sealed class NetworkResult<T> {
    class Success<T>(val data: T?) : NetworkResult<T>()
    class Error<T>(val message: String) : NetworkResult<T>()
    class Loading<T> : NetworkResult<T>()
}
```

**Example usage:**
```kotlin
when (val result = bookRepository.searchBooks("harry potter")) {
    is NetworkResult.Success -> {
        val books = result.data ?: emptyList()
        // Display books
    }
    is NetworkResult.Error -> {
        // Show error message
        val errorMessage = result.message
    }
    is NetworkResult.Loading -> {
        // Show loading indicator
    }
}
```

## Pagination Example

For implementing pagination/infinite scroll:

```kotlin
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books
    
    private var currentPage = 0
    private val pageSize = 20
    
    fun searchBooks(query: String) {
        currentPage = 0
        _books.value = emptyList()
        loadBooks(query)
    }
    
    fun loadMoreBooks(query: String) {
        currentPage++
        loadBooks(query)
    }
    
    private fun loadBooks(query: String) {
        viewModelScope.launch {
            val result = bookRepository.searchBooksWithPagination(
                query = query,
                page = currentPage,
                pageSize = pageSize
            )
            
            if (result is NetworkResult.Success) {
                val newBooks = result.data ?: emptyList()
                _books.value = _books.value + newBooks
            }
        }
    }
}
```

## Image Loading

To load images from URLs, consider using Coil library:

**Add to `build.gradle.kts`:**
```kotlin
implementation("io.coil-kt:coil-compose:2.5.0")
```

**Usage:**
```kotlin
import coil.compose.AsyncImage

AsyncImage(
    model = book.getCoverImageUrl("L"),
    contentDescription = book.title,
    placeholder = painterResource(R.drawable.placeholder),
    error = painterResource(R.drawable.error_image),
    modifier = Modifier.size(100.dp, 150.dp)
)
```

## API Rate Limits

OpenLibrary API has rate limits. Consider:
- Caching results locally
- Debouncing search queries
- Implementing retry logic for failed requests

## Additional Resources

- [OpenLibrary Search API Documentation](https://openlibrary.org/dev/docs/api/search)
- [OpenLibrary Covers API](https://openlibrary.org/dev/docs/api/covers)
- [OpenLibrary Author API](https://openlibrary.org/dev/docs/api/authors)
