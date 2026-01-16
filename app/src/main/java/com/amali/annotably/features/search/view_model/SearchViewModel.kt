package com.amali.annotably.features.search.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amali.annotably.data.model.Book
import com.amali.annotably.data.network.NetworkResult
import com.amali.annotably.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(private val bookRepository: BookRepository) :
        ViewModel() {

    companion object {
        private const val PAGE_SIZE = 10
    }

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)

    private val _loadingBookKeys = MutableStateFlow<String>("")
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    val loadingBookKeys: StateFlow<String> = _loadingBookKeys.asStateFlow()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _canLoadMore = MutableStateFlow(true)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var currentQuery: String = ""
    private var currentOffset: Int = 0

    fun searchBooks(query: String) {
        if (query.isBlank()) {
            _searchState.value = SearchUiState.Idle
            _books.value = emptyList()
            return
        }

        // Reset pagination for new search
        currentQuery = query
        currentOffset = 0
        _canLoadMore.value = true

        viewModelScope.launch {
            _searchState.value = SearchUiState.Loading

            when (val result = bookRepository.searchBooks(query, limit = PAGE_SIZE, offset = 0)) {
                is NetworkResult.Success -> {
                    val bookList = result.data ?: emptyList()
                    _books.value = bookList
                    currentOffset = bookList.size
                    _canLoadMore.value = bookList.size >= PAGE_SIZE
                    _searchState.value =
                            if (bookList.isEmpty()) {
                                SearchUiState.Empty
                            } else {
                                SearchUiState.Success
                            }
                }
                is NetworkResult.Error -> {
                    _searchState.value = SearchUiState.Error(result.message ?: "Unknown error")
                }
                is NetworkResult.Loading -> {
                    _searchState.value = SearchUiState.Loading
                }
            }
        }
    }

    fun loadMore() {
        if (_isLoadingMore.value || !_canLoadMore.value || currentQuery.isBlank()) {
            return
        }

        viewModelScope.launch {
            _isLoadingMore.value = true

            when (val result =
                            bookRepository.searchBooks(
                                    currentQuery,
                                    limit = PAGE_SIZE,
                                    offset = currentOffset
                            )
            ) {
                is NetworkResult.Success -> {
                    val newBooks = result.data ?: emptyList()
                    if (newBooks.isEmpty()) {
                        _canLoadMore.value = false
                    } else {
                        _books.value = _books.value + newBooks
                        currentOffset += newBooks.size
                        _canLoadMore.value = newBooks.size >= PAGE_SIZE
                    }
                }
                is NetworkResult.Error -> {
                    // Silently fail on load more, user can scroll up and retry
                }
                is NetworkResult.Loading -> {
                    // Already handled by isLoadingMore
                }
            }

            _isLoadingMore.value = false
        }
    }

    fun clearSearch() {
        _searchState.value = SearchUiState.Idle
        _books.value = emptyList()
        currentQuery = ""
        currentOffset = 0
        _canLoadMore.value = true
    }

    fun addBook(book: Book) {
        viewModelScope.launch {
            val bookKey = book.key
            Log.d("", "is Exist ${bookRepository.bookExists(book)}")


            // Check if book already exists
            if (bookRepository.bookExists(book) || bookKey.equals(_loadingBookKeys.value)) {
                _snackbarMessage.value = "Book already exists in your collection"
                _loadingBookKeys.value = ""

                return@launch
            }

            _loadingBookKeys.value = bookKey ?: ""

            // Add book if it doesn't exist
            when (val result = bookRepository.addBookToFirestore(book)) {
                is NetworkResult.Success -> {
                    _snackbarMessage.value = "Book added successfully"
                }
                is NetworkResult.Error -> {
                    _snackbarMessage.value = "Error: ${result.message ?: "Failed to add book"}"
                }
                is NetworkResult.Loading -> {
                    // Loading state if needed
                }
            }

            _loadingBookKeys.value = ""
        }
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
}

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data object Success : SearchUiState
    data object Empty : SearchUiState
    data class Error(val message: String) : SearchUiState
}
