package com.amali.annotably.features.home.view_model

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
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _homeState.value = HomeUiState.Loading

            when (val result = bookRepository.getAllBooksFromFirestore()) {
                is NetworkResult.Success -> {
                    val bookList = result.data ?: emptyList()
                    _books.value = bookList
                    _homeState.value = if (bookList.isEmpty()) {
                        HomeUiState.Empty
                    } else {
                        HomeUiState.Success
                    }
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

    fun refreshBooks() {
        viewModelScope.launch {
            _isRefreshing.value = true

            when (val result = bookRepository.getAllBooksFromFirestore()) {
                is NetworkResult.Success -> {
                    val bookList = result.data ?: emptyList()
                    _books.value = bookList
                    _homeState.value = if (bookList.isEmpty()) {
                        HomeUiState.Empty
                    } else {
                        HomeUiState.Success
                    }
                }
                is NetworkResult.Error -> {
                    // Don't change state on refresh error, just keep current state
                }
                is NetworkResult.Loading -> {
                    // Already refreshing
                }
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
