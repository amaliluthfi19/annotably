package com.amali.annotably.features.search.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amali.annotably.R
import com.amali.annotably.features.search.ui.components.BookItem
import com.amali.annotably.features.search.ui.components.LoadingListPlaceholder
import com.amali.annotably.features.search.ui.components.SearchTopBar
import com.amali.annotably.features.search.view_model.SearchUiState
import com.amali.annotably.features.search.view_model.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
        onBackClick: () -> Unit,
        modifier: Modifier = Modifier,
        viewModel: SearchViewModel = hiltViewModel()
) {
    var query by rememberSaveable { mutableStateOf("") }
    val searchState by viewModel.searchState.collectAsState()
    val loadingBookKeys by viewModel.loadingBookKeys.collectAsState()
    val books by viewModel.books.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val canLoadMore by viewModel.canLoadMore.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // Detect when scrolled near bottom to trigger load more
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 3 && totalItems > 0
        }
    }

    LaunchedEffect(shouldLoadMore, canLoadMore, isLoadingMore) {
        if (shouldLoadMore && canLoadMore && !isLoadingMore) {
            viewModel.loadMore()
        }
    }

    // Show snackbar when message is received
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            val isError = message.startsWith("Error:")
            snackbarHostState.showSnackbar(
                    message = message,
                    duration =
                            if (isError) {
                                SnackbarDuration.Long
                            } else {
                                SnackbarDuration.Short
                            }
            )
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
            topBar = {
                SearchTopBar(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = { viewModel.searchBooks(query) },
                        onBackClick = onBackClick,
                        onClear = { viewModel.clearSearch() }
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                    Snackbar(
                            snackbarData = snackbarData,
                            containerColor =
                                    if (snackbarData.visuals.message.startsWith("Error:")) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.inverseSurface
                                    },
                            contentColor =
                                    if (snackbarData.visuals.message.startsWith("Error:")) {
                                        MaterialTheme.colorScheme.onError
                                    } else {
                                        MaterialTheme.colorScheme.inverseOnSurface
                                    }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.onPrimary,
            modifier = modifier
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (searchState) {
                is SearchUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                                painter = painterResource(R.drawable.empty_state_illustration),
                                contentDescription = "Empty state illustration",
                                modifier = Modifier.size(240.dp)
                        )
                    }
                }
                is SearchUiState.Loading -> {
                    LoadingListPlaceholder()
                }
                is SearchUiState.Success -> {
                    LazyColumn(state = listState) {
                        item {
                            Text(
                                    text = "Results",
                                    style = typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                            )
                        }
                        items(books) { book ->
                            BookItem(
                                    book = book,
                                    onAddBook = { viewModel.addBook(book) },
                                    isLoading = loadingBookKeys.equals(book.key)
                            )
                        }

                        // Loading more indicator
                        if (isLoadingMore) {
                            item {
                                Box(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
                is SearchUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                                text = "No books found",
                                style = typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is SearchUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                                text = (searchState as SearchUiState.Error).message,
                                style = typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
