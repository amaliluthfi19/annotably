package com.amali.annotably.features.home.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amali.annotably.data.model.Book
import com.amali.annotably.data.network.NetworkResult
import com.amali.annotably.data.repository.BookRepository
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var lastDocument: DocumentSnapshot? = null
    private var hasMore = true

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _homeState.value = HomeUiState.Loading
            lastDocument = null
            hasMore = true

            when (val result = bookRepository.getPaginatedBook(null)) {
                is NetworkResult.Success -> {
                    val page = result.data!!
                    lastDocument = page.lastDocument
                    hasMore = page.hasMore
                    _books.value = page.books
                    _homeState.value = if (page.books.isEmpty()) HomeUiState.Empty else HomeUiState.Success
                }
                is NetworkResult.Error -> {
                    _homeState.value = HomeUiState.Error(result.message ?: "Unknown error")
                }
                is NetworkResult.Loading -> {
                    _homeState.value = HomeUiState.Loading
                }
            }
        }
    }

    fun loadMoreBooks() {
        if (!hasMore || _isLoadingMore.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true

            when (val result = bookRepository.getPaginatedBook(lastDocument)) {
                is NetworkResult.Success -> {
                    val page = result.data!!
                    lastDocument = page.lastDocument
                    hasMore = page.hasMore
                    _books.value = _books.value + page.books
                }
                is NetworkResult.Error -> {
                    // Keep current books, pagination will retry on next trigger
                }
                is NetworkResult.Loading -> {}
            }

            _isLoadingMore.value = false
        }
    }

    fun refreshBooks() {
        viewModelScope.launch {
            _isRefreshing.value = true
            lastDocument = null
            hasMore = true

            when (val result = bookRepository.getPaginatedBook(null)) {
                is NetworkResult.Success -> {
                    val page = result.data!!
                    lastDocument = page.lastDocument
                    hasMore = page.hasMore
                    _books.value = page.books
                    _homeState.value = if (page.books.isEmpty()) HomeUiState.Empty else HomeUiState.Success
                }
                is NetworkResult.Error -> {
                    // Keep current state on refresh error
                }
                is NetworkResult.Loading -> {}
            }

            _isRefreshing.value = false
        }
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Success : HomeUiState
    data object Empty : HomeUiState
    data class Error(val message: String) : HomeUiState
}
